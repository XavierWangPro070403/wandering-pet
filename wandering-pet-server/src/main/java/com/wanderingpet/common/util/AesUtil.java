package com.wanderingpet.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES 工具：AES-128-CBC / PKCS5Padding，密钥同时作为 IV（与种子数据加密方式一致）
 *
 * <p>密钥由 {@link KeyHolder} 在 Spring 装配时注入（TypeHandler 无法直接注入 Spring Bean）。</p>
 */
public final class AesUtil {

    private AesUtil() {
    }

    /**
     * 静态密钥持有者：由配置类在启动时写入
     */
    public static class KeyHolder {
        private static volatile String KEY = "wanderingPet2026";
    }

    public static void setKey(String key) {
        if (key != null && key.length() == 16) {
            KeyHolder.KEY = key;
        }
    }

    /** 加密：明文 → AES 密文 → Base64 */
    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) {
            return null;
        }
        try {
            byte[] keyBytes = KeyHolder.KEY.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(keyBytes));
            byte[] encrypted = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalStateException("AES 加密失败", e);
        }
    }

    /** 解密：Base64 → AES 密文 → 明文；密文为空或解密失败返回 null */
    public static String decrypt(String base64) {
        if (base64 == null || base64.isEmpty()) {
            return null;
        }
        try {
            byte[] keyBytes = KeyHolder.KEY.getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(keyBytes));
            byte[] plain = cipher.doFinal(Base64.getDecoder().decode(base64));
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
