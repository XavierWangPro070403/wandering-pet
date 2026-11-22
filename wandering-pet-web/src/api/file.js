import request from '@/utils/request';

/**
 * 上传文件（MinIO）
 * @param {File} file 文件
 * @param {string} bizType 业务类型 pet/avatar/article/donation/report/agreement/dynamic
 * @returns {Promise<{objectName:string, url:string}>}
 */
export function uploadFile(file, bizType = 'pet') {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('bizType', bizType);
  return request.post('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  });
}
