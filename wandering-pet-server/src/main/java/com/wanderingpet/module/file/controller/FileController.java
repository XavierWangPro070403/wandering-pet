package com.wanderingpet.module.file.controller;

import com.wanderingpet.common.annotation.OpLog;
import com.wanderingpet.common.annotation.RequirePermission;
import com.wanderingpet.common.result.R;
import com.wanderingpet.module.file.service.FileService;
import com.wanderingpet.module.file.vo.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口
 */
@Tag(name = "文件", description = "图片/视频/协议 PDF 上传（MinIO）")
@RestController
@RequestMapping("/api/v1/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "上传文件")
    @RequirePermission("file:upload")
    @PostMapping("/upload")
    @OpLog(module = "file", action = "上传文件")
    public R<FileVO> upload(@RequestParam("file") MultipartFile file,
                            @Parameter(description = "业务类型：pet/avatar/article/donation/report/agreement/dynamic")
                            @RequestParam(defaultValue = "pet") String bizType) {
        return R.ok(fileService.upload(file, bizType));
    }
}
