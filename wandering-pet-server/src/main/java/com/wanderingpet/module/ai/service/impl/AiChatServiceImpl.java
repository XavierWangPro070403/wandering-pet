package com.wanderingpet.module.ai.service.impl;

import com.wanderingpet.common.exception.BizException;
import com.wanderingpet.common.result.ResultCode;
import com.wanderingpet.config.props.DeepSeekProperties;
import com.wanderingpet.module.ai.dto.AiChatDTO;
import com.wanderingpet.module.ai.service.AiChatService;
import com.wanderingpet.module.ai.vo.AiReplyVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI 宠物顾问实现：调用 DeepSeek（OpenAI 兼容接口）
 *
 * <p>api-key 未配置时进入演示模式：命中本地知识库关键词则返回预置回答，
 * 否则返回引导文案，保证页面在无 Key 环境下依旧可用。</p>
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private final DeepSeekProperties deepSeekProperties;
    private final RestTemplate restTemplate;

    /** 系统提示词：限定宠物顾问角色与回答风格 */
    private static final String SYSTEM_PROMPT = """
            你是"流浪宠物领养平台"的 AI 宠物顾问，名叫小爪。用户会咨询流浪动物救助、领养流程、
            猫狗养护（饮食/疫苗/驱虫/绝育/行为训练）、健康判断等问题。回答要求：
            1. 使用简体中文，语气亲切友好，可以适当用 emoji；
            2. 回答简洁实用，控制在 250 字以内，分点时用「1. 2. 3.」；
            3. 涉及疾病症状时提醒用户"建议尽快就医"；
            4. 与宠物无关的问题，友好地引导回宠物话题；
            5. 平台相关的领养流程问题：注册登录 → 选择宠物提交申请 → 救助站初筛 → 面谈 → 家访 → 签约领养 → 定期回访。
            """;

    /** 演示模式本地知识库：关键词 → 预置回答 */
    private static final Map<String, String> DEMO_KB = Map.ofEntries(
            Map.entry("领养", "平台领养流程很简单：\n1. 登录后在「待领养」挑选喜欢的宠物；\n2. 点「申请领养」填写资料（会自动带入你的领养资料）；\n3. 救助站 3 个工作日内初筛，通过后安排面谈、家访；\n4. 签署领养协议完成领养，之后第 7/30/90/180 天有回访。\n有心仪的宠物可以直接去它的档案页点「申请领养」哦🐾"),
            Map.entry("疫苗", "幼猫幼犬疫苗建议：\n1. 猫：8 周龄起打猫三联，共 2-3 针，每针间隔 3-4 周；\n2. 狗：45 日龄起打犬联苗，3 月龄以上接种狂犬疫苗；\n3. 成年后每年加强一针。\n接种前后一周不要洗澡，接种当天避免应激。如果宠物出现精神萎靡、呕吐等症状，建议尽快就医。"),
            Map.entry("绝育", "绝育的好处：\n1. 降低生殖系统疾病风险（母猫子宫蓄脓、公狗前列腺问题等）；\n2. 减少发情期嚎叫、走失、标记行为；\n3. 流浪动物数量控制的根本方式。\n建议猫 6 月龄后、狗 6-9 月龄后进行，术前需体检确认可以麻醉。"),
            Map.entry("驱虫", "驱虫建议：\n1. 体外驱虫（跳蚤/蜱虫）：每月一次滴剂；\n2. 体内驱虫（蛔虫/绦虫）：幼宠每月一次，成宠每 3 个月一次；\n3. 领养流浪动物建议先到宠物医院做一次全面粪检与皮肤检查。\n如果发现宠物频繁抓挠、便便里有虫体，建议尽快就医。"),
            Map.entry("流浪", "遇到流浪动物可以这样做：\n1. 打开平台「流浪上报」提交位置与照片，救助站会尽快跟进；\n2. 如有伤病，可先联系就近救助站；\n3. 投喂请选择正规猫粮/狗粮并提供清水；\n4. 想帮助它，也可以在「爱心捐赠」支持救助站的救助项目🐾"),
            Map.entry("喂", "喂养建议：\n1. 幼猫/幼犬吃专用幼宠粮，少食多餐（一天 3-4 次）；\n2. 成猫/成犬定时定量，一天 2 次；\n3. 禁食巧克力、洋葱、葡萄、木糖醇口香糖等（对猫狗有毒）；\n4. 换粮要 7 天过渡，突然换粮容易腹泻。")
    );

    private static final String DEMO_FALLBACK = "我是 AI 宠物顾问小爪🐾 目前运行在演示模式下（管理员在 application.yml 配置 DeepSeek API Key 后我就能深度回答啦）。\n你可以先问我这些：领养流程、疫苗接种、驱虫绝育、流浪动物救助、喂养注意事项～";

    public AiChatServiceImpl(DeepSeekProperties deepSeekProperties) {
        this.deepSeekProperties = deepSeekProperties;
        var factory = new org.springframework.http.client.SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(5000);
        factory.setReadTimeout(30000);
        this.restTemplate = new RestTemplate(factory);
    }

    @Override
    public AiReplyVO chat(AiChatDTO dto) {
        String message = dto.getMessage() == null ? "" : dto.getMessage().trim();
        if (message.isBlank()) {
            throw BizException.of(ResultCode.BAD_REQUEST, "请输入你的问题");
        }
        if (message.length() > 1000) {
            throw BizException.of(ResultCode.BAD_REQUEST, "问题太长啦，请精简到 1000 字以内");
        }
        if (deepSeekProperties.devMode()) {
            return demoReply(message);
        }
        return deepseekReply(dto, message);
    }

    /** 真实调用 DeepSeek（OpenAI 兼容 /chat/completions） */
    private AiReplyVO deepseekReply(AiChatDTO dto, String message) {
        try {
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));
            if (dto.getHistory() != null) {
                dto.getHistory().stream()
                        .filter(h -> h.getRole() != null && h.getContent() != null)
                        .limit(6)
                        .forEach(h -> messages.add(Map.of("role", h.getRole(), "content", h.getContent())));
            }
            messages.add(Map.of("role", "user", "content", message));

            Map<String, Object> body = new HashMap<>();
            body.put("model", deepSeekProperties.getModel());
            body.put("messages", messages);
            body.put("temperature", 0.7);
            body.put("max_tokens", 800);

            var headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(deepSeekProperties.getApiKey());

            var request = new org.springframework.http.HttpEntity<>(body, headers);
            String url = deepSeekProperties.getBaseUrl().replaceAll("/$", "") + "/chat/completions";
            Map<?, ?> result = restTemplate.postForObject(url, request, Map.class);

            List<?> choices = result == null ? null : (List<?>) result.get("choices");
            if (choices == null || choices.isEmpty()) {
                throw new IllegalStateException("DeepSeek 返回为空");
            }
            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Map<?, ?> msg = (Map<?, ?>) first.get("message");
            String reply = msg == null ? null : String.valueOf(msg.get("content"));
            if (reply == null || reply.isBlank()) {
                throw new IllegalStateException("DeepSeek 回复为空");
            }
            AiReplyVO vo = new AiReplyVO();
            vo.setReply(reply.trim());
            vo.setDemo(false);
            return vo;
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            log.error("DeepSeek 调用失败", e);
            AiReplyVO vo = demoReply(message);
            vo.setReply("（AI 服务暂时不可用，以下为演示回复）\n\n" + vo.getReply());
            vo.setDemo(true);
            return vo;
        }
    }

    /** 演示模式：本地知识库关键词匹配 */
    private AiReplyVO demoReply(String message) {
        AiReplyVO vo = new AiReplyVO();
        vo.setDemo(true);
        String hit = DEMO_KB.entrySet().stream()
                .filter(e -> message.contains(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEMO_FALLBACK);
        vo.setReply(hit);
        return vo;
    }
}
