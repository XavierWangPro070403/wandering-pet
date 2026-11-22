# 演示图片素材来源（photo-credits）

> 用途：开发演示数据（种子库 12 只宠物档案、8 篇资讯、2 个募捐项目、3 条线索等）。
> 页面本身不加"示例"标签（提示词 §8.9）；本文件是素材追溯的唯一位置。
> 图片由后端静态目录 `wandering-pet-server/src/main/resources/static/imgs/**` 托管（`http://localhost:8080/imgs/**`）。

## 来源与许可

| 来源 | 数量 | 许可 |
|---|---|---|
| Unsplash（images.unsplash.com 直链，w=800&h=600&fit=crop&q=75） | 约 44 张 | Unsplash License（可商用、无需署名；此处仍主动署名） |
| Wikimedia Commons（API 检索 + Special:FilePath，宽度 800） | 约 22 张 | CC BY-SA / CC0 / 公有领域（以 Commons 各文件页标注为准） |
| dog.ceo API（Stanford Dogs Dataset 子集） | 约 3 张 | MIT（数据集原始许可见 dataset 页） |

同一张照片在两处位置重复出现的情况：无（各照片仅出现一次；相册 3 张与封面互不重复）。

## 文件清单（按目录）

- `pet/p01–p48.jpg`：12 只演示宠物的封面 + 各 3 张相册（p01-p36 为封面与相册 1-2，p37-p48 为相册 3）
- `article/a01–a08.jpg`：8 篇资讯封面
- `donation/d01–d02.jpg`：2 个募捐项目封面
- `report/r01–r03.jpg`：3 条流浪线索现场图
- `followup/f01–f03.jpg`：第 1 次回访生活照
- `shelter/s01–s03.jpg`：3 家演示机构 LOGO 位（演示用途）
- `avatar/a01–a03.jpg`：平台管理员与 2 位演示用户头像
- `icon/cat|dog|rabbit|other.png`：物种分类图标（本工程程序化生成，自有）

> 替换说明：生产使用时请以真实拍摄素材替换，并在此文件登记拍摄者与授权；
> 批量替换时保持文件名不变即可，无需改动种子数据。

## 主要 Unsplash 素材对应（示例）

- 宠物封面：p01 橘猫、p04 狸花猫、p07 白猫、p10 金毛、p13 黑猫、p16 三花猫、p19 柯基、p22 贵宾、p25 柴犬、p28 哈士奇、p31 垂耳兔、p34 田园犬
- Web 门户首屏 `hero/hero-main.jpg` 取自 p10（金毛）素材的独立副本，仅用于首屏背景（未与其他位置同图）

## 下载方式留档

下载脚本（临时目录执行，不入库）：
- Unsplash：`https://images.unsplash.com/photo-{id}?w=800&h=600&fit=crop&q=75`
- Commons：`https://commons.wikimedia.org/wiki/Special:FilePath/{File}.jpg?width=800`
- dog.ceo：`https://dog.ceo/api/breed/{breed}/images/random`
