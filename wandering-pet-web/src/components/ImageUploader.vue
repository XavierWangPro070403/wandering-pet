<template>
  <div class="image-uploader">
    <el-upload
      ref="upload"
      action=""
      list-type="picture-card"
      accept="image/jpeg,image/png,image/webp"
      :http-request="doUpload"
      :file-list="fileList"
      :limit="limit"
      :on-remove="onRemove"
      :on-exceed="() => $message.warning(`最多上传 ${limit} 张`)"
      multiple>
      <i class="el-icon-plus" aria-hidden="true"></i>
    </el-upload>
    <p class="image-uploader__tip">支持 jpg / png / webp，单张 ≤ 5MB，最多 {{ limit }} 张</p>
  </div>
</template>

<script>
import { uploadFile } from '@/api/file';

/**
 * 九宫格图片上传（§8.6 上传组件）：上传到 MinIO，v-model 为 [{objectName, url}]
 */
export default {
  name: 'ImageUploader',
  props: {
    value: { type: Array, default: () => [] },
    bizType: { type: String, default: 'pet' },
    limit: { type: Number, default: 9 }
  },
  computed: {
    fileList() {
      return (this.value || []).map((item, index) => ({
        uid: `img-${index}-${item.objectName}`,
        name: item.objectName || item.url,
        url: item.url,
        status: 'success'
      }));
    }
  },
  methods: {
    async doUpload({ file, onError, onSuccess }) {
      try {
        const data = await uploadFile(file, this.bizType);
        const next = [...(this.value || []), { objectName: data.objectName, url: data.url }];
        this.$emit('input', next);
        onSuccess(data);
      } catch (e) {
        onError(e);
      }
    },
    onRemove(file) {
      const next = (this.value || []).filter((item) => item.url !== file.url);
      this.$emit('input', next);
    }
  }
};
</script>

<style lang="scss" scoped>
@import '~@/styles/variables.scss';

.image-uploader {
  &__tip {
    font-size: 12px;
    color: $text-secondary;
    line-height: 20px;
  }

  ::v-deep .el-upload--picture-card,
  ::v-deep .el-upload-list--picture-card .el-upload-list__item {
    width: 96px;
    height: 96px;
    border-radius: $radius-base;
    border-color: $border-color;
  }

  // Element 缩略图默认 width/height 100% 但没有 object-fit，
  // 非方形原图会被拉伸变形；统一裁切填充并居中
  ::v-deep .el-upload-list--picture-card .el-upload-list__item img {
    object-fit: cover;
    object-position: center;
  }

  ::v-deep .el-upload--picture-card {
    // 居中加号：Element 默认给 picture-card 设了 146px 行高，会把图标顶偏下
    display: flex;
    align-items: center;
    justify-content: center;
    line-height: 1;
    border-color: $color-primary;
    border-style: dashed;
    background: $color-primary-light;
    transition: border-color $duration-hover-in $ease-out-quart,
                background-color $duration-hover-in $ease-out-quart;

    &:hover {
      border-color: $color-primary-active;
      background: #FFE9DF;
    }

    i {
      font-size: 26px;
      color: $color-primary-text;
      font-weight: 600;
    }
  }

  ::v-deep .el-upload-list--picture-card .el-upload-list__item:hover {
    border-color: $color-primary;
  }
}
</style>
