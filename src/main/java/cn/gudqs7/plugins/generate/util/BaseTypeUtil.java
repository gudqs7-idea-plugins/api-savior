package cn.gudqs7.plugins.generate.util;

import cn.gudqs7.plugins.docer.constant.CommentConst;
import cn.gudqs7.plugins.docer.pojo.annotation.CommentInfo;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiType;
import lombok.Data;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;

/**
 * @author wenquan
 * @date 2021/9/30
 */
public class BaseTypeUtil {

    private static final Map<String, TypeInfo> JAVA_BASE_TYPE_MAP = new HashMap<>(32);
    private static final Map<String, TypeInfo> OTHER_BASE_TYPE_MAP = new HashMap<>(32);
    private static final Map<String, TypeInfo> OTHER_INTERFACE_MAP = new HashMap<>(32);

    static {
        // 此处处理不能直接 new 的类型, 也就是接口, 常用的接口目前只想到三大集合
        OTHER_INTERFACE_MAP.put("java.util.List", TypeInfo.of("java.util.ArrayList", "new ArrayList<>()", new ArrayList<>()));
        OTHER_INTERFACE_MAP.put("java.util.Map", TypeInfo.of("java.util.HashMap", "new HashMap<>(2)", new HashMap<>(2)));
        OTHER_INTERFACE_MAP.put("java.util.Set", TypeInfo.of("java.util.HashSet", "new HashSet<>()", new HashSet<>(2)));
        OTHER_INTERFACE_MAP.put("java.util.Collection", TypeInfo.of("java.util.ArrayList", "new ArrayList<>()", new ArrayList<>()));

        // other base
        OTHER_BASE_TYPE_MAP.put("java.math.BigDecimal", TypeInfo.of("java.math.BigDecimal", "new BigDecimal(0)", commentInfo->{
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? BigDecimal.valueOf(RandomUtils.nextDouble(50, 1000)) : new BigDecimal(example);
        }));
        OTHER_BASE_TYPE_MAP.put("java.util.Date", TypeInfo.of("java.util.Date", "new Date()", randomDate()));
        OTHER_BASE_TYPE_MAP.put("java.sql.Date", TypeInfo.of("java.sql.Date", "new Date(System.currentTimeMillis())", randomDate()));
        OTHER_BASE_TYPE_MAP.put("java.sql.Timestamp", TypeInfo.of("java.sql.Timestamp", "new Timestamp(System.currentTimeMillis())", randomDate()));
        OTHER_BASE_TYPE_MAP.put("java.sql.Time", TypeInfo.of("java.sql.Time", "new Time(System.currentTimeMillis())", randomDate()));
        Function<CommentInfo, Object> stringGetFn = commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomString(commentInfo) : example;
        };
        OTHER_BASE_TYPE_MAP.put("java.sql.Blob", TypeInfo.of("javax.sql.rowset.serial.SerialBlob", "new SerialBlob(new byte[]{})", stringGetFn));
        OTHER_BASE_TYPE_MAP.put("java.sql.Clob", TypeInfo.of("javax.sql.rowset.serial.SerialClob.SerialClob", "new SerialClob(new char[]{})", stringGetFn));
        OTHER_BASE_TYPE_MAP.put("java.sql.NClob", TypeInfo.of("javax.sql.rowset.serial.SerialClob.SerialClob", "new SerialClob(new char[]{})", stringGetFn));
        OTHER_BASE_TYPE_MAP.put("org.springframework.web.multipart.MultipartFile", TypeInfo.of( "null", stringGetFn));

        // java base
        JAVA_BASE_TYPE_MAP.put("byte", TypeInfo.of("(byte) 0", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? RandomUtils.nextBytes(1)[0] : Byte.parseByte(example);
        }));
        JAVA_BASE_TYPE_MAP.put("short", TypeInfo.of("(short) 0", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomShort() : Short.parseShort(example);
        }));
        TypeInfo typeInfoForInt = TypeInfo.of("0", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? RandomUtils.nextInt(1, 128) : Integer.parseInt(example);
        });
        JAVA_BASE_TYPE_MAP.put("int", typeInfoForInt);
        JAVA_BASE_TYPE_MAP.put("integer", typeInfoForInt);
        JAVA_BASE_TYPE_MAP.put("number", TypeInfo.of("0", commentInfo -> 0));
        TypeInfo typeInfoForChar = TypeInfo.of("'0'", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomChar() : example.charAt(0);
        });
        JAVA_BASE_TYPE_MAP.put("char", typeInfoForChar);
        JAVA_BASE_TYPE_MAP.put("character", typeInfoForChar);
        JAVA_BASE_TYPE_MAP.put("boolean", TypeInfo.of("false", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? RandomUtils.nextBoolean() : Boolean.parseBoolean(example);
        }));
        JAVA_BASE_TYPE_MAP.put("double", TypeInfo.of("0D", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? RandomUtils.nextDouble(50, 1000) : Double.parseDouble(example);
        }));
        JAVA_BASE_TYPE_MAP.put("float", TypeInfo.of("0f", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? RandomUtils.nextFloat(10, 100) : Float.parseFloat(example);
        }));
        JAVA_BASE_TYPE_MAP.put("long", TypeInfo.of("0L", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? RandomUtils.nextLong(10, 1000) : Long.parseLong(example);
        }));
        JAVA_BASE_TYPE_MAP.put("string", TypeInfo.of("\"\"", stringGetFn));
    }

    public static Object getDefaultVal(PsiType psiType, CommentInfo commentInfo) {
        if (psiType == null) {
            return null;
        }
        String typeName = psiType.getPresentableText();
        String qName = psiType.getCanonicalText();
        TypeInfo typeInfo = JAVA_BASE_TYPE_MAP.get(typeName);
        if (typeInfo == null) {
            typeInfo = OTHER_BASE_TYPE_MAP.get(qName);
        }
        if (typeInfo != null) {
            return typeInfo.getDefaultValGetFn().apply(commentInfo);
        }
        return null;
    }

    public static boolean isBaseTypeOrObject(PsiType psiType) {
        if (psiType == null) {
            return false;
        }
        String qName = psiType.getCanonicalText();
        String typeName = psiType.getPresentableText();
        return isBaseType(typeName, qName) || typeIsObject(typeName);
    }

    public static boolean isBaseTypeOrObject(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        String typeName = psiClass.getName();
        String qName = psiClass.getQualifiedName();
        return isBaseType(typeName, qName) || typeIsObject(typeName);
    }

    public static boolean isJavaBaseTypeOrObject(String typeName) {
        return isJavaBaseType(typeName) || typeIsObject(typeName);
    }

    public static boolean isJavaBaseType(String typeName) {
        if (typeName == null) {
            return false;
        }
        return JAVA_BASE_TYPE_MAP.containsKey(typeName.toLowerCase());
    }

    public static boolean isOtherBaseType(String qName) {
        if (qName == null) {
            return false;
        }
        return OTHER_BASE_TYPE_MAP.containsKey(qName);
    }

    public static boolean isBaseType(String typeName, String qName) {
        return isJavaBaseType(typeName) || isOtherBaseType(qName);
    }

    public static String getJavaBaseTypeDefaultValStr(String typeName) {
        TypeInfo typeInfo = JAVA_BASE_TYPE_MAP.get(typeName);
        if (typeInfo == null) {
            return null;
        }
        return typeInfo.getDefaultValStr();
    }

    public static String getCommonDefaultVal(String qName) {
        TypeInfo typeInfo = getTypeInfoByQName(qName);
        if (typeInfo == null) {
            return null;
        }
        return typeInfo.getDefaultValStr();
    }

    public static String getCommonDefaultValImport(String qName) {
        TypeInfo typeInfo = getTypeInfoByQName(qName);
        if (typeInfo == null) {
            return null;
        }
        return typeInfo.getImportStr();
    }

    public static TypeInfo getTypeInfoByQName(String qName) {
        TypeInfo typeInfo = OTHER_BASE_TYPE_MAP.get(qName);
        if (typeInfo == null) {
            typeInfo = OTHER_INTERFACE_MAP.get(qName);
        }
        return typeInfo;
    }


    private static boolean typeIsObject(String typeName) {
        return "Object".equals(typeName);
    }

    private static String randomDate() {
        Date now = new Date();
        now.setTime(System.currentTimeMillis() + RandomUtils.nextLong(0, 86400000));
        return DateFormatUtils.formatUTC(now, "yyyy-MM-dd'T'HH:mm:ss.SSS+0000");
    }

    private static String randomString(CommentInfo commentInfo) {
        String fieldDesc = commentInfo.getValue("");
        Boolean random = commentInfo.getSingleBool("random", false);
        Boolean guid = commentInfo.getSingleBool("guid", false);
        if (guid) {
            return UUID.randomUUID().toString().toUpperCase();
        }
        if (random || StringUtils.isBlank(fieldDesc)) {
            int length = RandomUtils.nextInt(5, 20);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                stringBuilder.append(randomChar(false));
            }
            return stringBuilder.toString();
        } else {
            if (fieldDesc.contains(CommentConst.BREAK_LINE)) {
                fieldDesc = fieldDesc.substring(0, fieldDesc.indexOf(CommentConst.BREAK_LINE));
            }
            return fieldDesc + RandomUtils.nextInt(1, 128);
        }
    }

    private static short randomShort() {
        return (short) RandomUtils.nextInt(1, 100);
    }

    private static char randomChar() {
        boolean en = RandomUtils.nextBoolean();
        return randomChar(en);
    }

    private static char randomChar(boolean en) {
        if (en) {
            boolean upper = RandomUtils.nextBoolean();
            if (upper) {
                return (char) RandomUtils.nextInt(65, 90);
            } else {
                return (char) RandomUtils.nextInt(97, 122);
            }
        } else {
            String str = "";
            int highCode;
            int lowCode;

            Random random = new Random();

            //B0 + 0~39(16~55) 一级汉字所占区
            highCode = (176 + Math.abs(random.nextInt(39)));
            //A1 + 0~93 每区有94个汉字
            lowCode = (161 + Math.abs(random.nextInt(93)));

            byte[] b = new byte[2];
            b[0] = (Integer.valueOf(highCode)).byteValue();
            b[1] = (Integer.valueOf(lowCode)).byteValue();

            try {
                str = new String(b, "GBK");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            return str.charAt(0);
        }
    }

    @Data
    public static class TypeInfo {

        private String importStr;
        private String defaultValStr;
        private Function<CommentInfo, Object> defaultValGetFn;

        public TypeInfo(String importStr, String defaultValStr, Function<CommentInfo, Object> defaultValGetFn) {
            this.importStr = importStr;
            this.defaultValStr = defaultValStr;
            this.defaultValGetFn = defaultValGetFn;
        }

        public static TypeInfo of(String importStr, String defaultValStr, Object defaultVal) {
            return of(importStr, defaultValStr, (commentInfo) -> defaultVal);
        }

        public static TypeInfo of(String defaultValStr, Function<CommentInfo, Object> defaultValGetFn) {
            return of(null, defaultValStr, defaultValGetFn);
        }

        public static TypeInfo of(String importStr, String defaultValStr, Function<CommentInfo, Object> defaultValGetFn) {
            return new TypeInfo(importStr, defaultValStr, defaultValGetFn);
        }
    }

}
