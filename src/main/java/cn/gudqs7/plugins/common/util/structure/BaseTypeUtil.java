package cn.gudqs7.plugins.common.util.structure;

import cn.gudqs7.plugins.common.consts.CommonConst;
import cn.gudqs7.plugins.common.enums.MoreCommentTagEnum;
import cn.gudqs7.plugins.common.enums.PluginSettingEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.util.PluginSettingHelper;
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
        Function<CommentInfo, Object> stringGetFn = commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomString(commentInfo) : example;
        };
        Function<CommentInfo, Object> dateGetFn = commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomDate(commentInfo) : example;
        };
        // 此处处理不能直接 new 的类型, 也就是接口, 常用的接口目前只想到三大集合
        OTHER_INTERFACE_MAP.put("java.util.List", TypeInfo.of("java.util.ArrayList", "new ArrayList<>()", new ArrayList<>()));
        OTHER_INTERFACE_MAP.put("java.util.Map", TypeInfo.of("java.util.HashMap", "new HashMap<>(2)", new HashMap<>(2)));
        OTHER_INTERFACE_MAP.put("java.util.Set", TypeInfo.of("java.util.HashSet", "new HashSet<>()", new HashSet<>(2)));
        OTHER_INTERFACE_MAP.put("java.util.Collection", TypeInfo.of("java.util.ArrayList", "new ArrayList<>()", new ArrayList<>()));
        OTHER_INTERFACE_MAP.put("org.springframework.web.multipart.MultipartFile", TypeInfo.of("null", stringGetFn));

        // other base
        OTHER_BASE_TYPE_MAP.put("java.lang.Number", TypeInfo.of("0", commentInfo -> 0));
        OTHER_BASE_TYPE_MAP.put("java.math.BigDecimal", TypeInfo.of("java.math.BigDecimal", "new BigDecimal(0)", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? BigDecimal.valueOf(randomDouble()) : new BigDecimal(example);
        }));
        OTHER_BASE_TYPE_MAP.put("java.util.Date", TypeInfo.of("java.util.Date", "new Date()", dateGetFn));
        OTHER_BASE_TYPE_MAP.put("java.sql.Date", TypeInfo.of("java.sql.Date", "new Date(System.currentTimeMillis())", dateGetFn));
        OTHER_BASE_TYPE_MAP.put("java.sql.Timestamp", TypeInfo.of("java.sql.Timestamp", "new Timestamp(System.currentTimeMillis())", dateGetFn));
        OTHER_BASE_TYPE_MAP.put("java.sql.Time", TypeInfo.of("java.sql.Time", "new Time(System.currentTimeMillis())", dateGetFn));

        OTHER_BASE_TYPE_MAP.put("java.sql.Blob", TypeInfo.of("javax.sql.rowset.serial.SerialBlob", "new SerialBlob(new byte[]{})", stringGetFn));
        OTHER_BASE_TYPE_MAP.put("java.sql.Clob", TypeInfo.of("javax.sql.rowset.serial.SerialClob", "new SerialClob(new char[]{})", stringGetFn));


        // java base
        JAVA_BASE_TYPE_MAP.put("byte", TypeInfo.of("(byte) 0", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomByte() : Byte.parseByte(example);
        }));
        JAVA_BASE_TYPE_MAP.put("short", TypeInfo.of("(short) 0", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomShort() : Short.parseShort(example);
        }));
        TypeInfo typeInfoForInt = TypeInfo.of("0", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomInt() : Integer.parseInt(example);
        });
        JAVA_BASE_TYPE_MAP.put("int", typeInfoForInt);
        JAVA_BASE_TYPE_MAP.put("integer", typeInfoForInt);
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
            return noExampleValue ? randomBoolean() : Boolean.parseBoolean(example);
        }));
        JAVA_BASE_TYPE_MAP.put("double", TypeInfo.of("0D", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomDouble() : Double.parseDouble(example);
        }));
        JAVA_BASE_TYPE_MAP.put("float", TypeInfo.of("0f", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomFloat() : Float.parseFloat(example);
        }));
        JAVA_BASE_TYPE_MAP.put("long", TypeInfo.of("0L", commentInfo -> {
            String example = commentInfo.getExample("");
            boolean noExampleValue = StringUtils.isBlank(example);
            return noExampleValue ? randomLong() : Long.parseLong(example);
        }));
        JAVA_BASE_TYPE_MAP.put("string", TypeInfo.of("\"\"", stringGetFn));
    }

    /**
     * 根据类型和全限定名判断是否为两种基础类型或 Object
     *
     * @param psiType 类型和全限定名
     * @return 是否为两种基础类型或 Object
     */
    public static boolean isBaseTypeOrObject(PsiType psiType) {
        if (psiType == null) {
            return false;
        }
        String qName = psiType.getCanonicalText();
        String typeName = psiType.getPresentableText();
        return isBaseType(typeName, qName) || typeIsObject(typeName);
    }

    /**
     * 根据类型和全限定名判断是否为两种基础类型或 Object
     *
     * @param psiClass 类型和全限定名
     * @return 是否为两种基础类型或 Object
     */
    public static boolean isBaseTypeOrObject(PsiClass psiClass) {
        if (psiClass == null) {
            return false;
        }
        String typeName = psiClass.getName();
        String qName = psiClass.getQualifiedName();
        return isBaseType(typeName, qName) || typeIsObject(typeName);
    }

    /**
     * 根据类型判断是否为Java基本类型或 Object
     *
     * @param typeName 类型
     * @return 是否为Java基本类型或 Object
     */
    public static boolean isJavaBaseTypeOrObject(String typeName) {
        return isJavaBaseType(typeName) || typeIsObject(typeName);
    }

    /**
     * 根据类型和全限定名判断是否为两种基础类型
     *
     * @param typeName 类型
     * @param qName    全限定名
     * @return 是否为两种基础类型
     */
    public static boolean isBaseType(String typeName, String qName) {
        return isJavaBaseType(typeName) || isOtherBaseType(qName);
    }

    /**
     * 根据类型判断是否为Java基本类型
     *
     * @param typeName 类型
     * @return 是否为Java基本类型
     */
    public static boolean isJavaBaseType(String typeName) {
        if (typeName == null) {
            return false;
        }
        return JAVA_BASE_TYPE_MAP.containsKey(typeName.toLowerCase());
    }

    /**
     * 根据全限定名判断是否为其他基本类型
     *
     * @param qName 全限定名
     * @return 是否为其他基本类型
     */
    public static boolean isOtherBaseType(String qName) {
        if (qName == null) {
            return false;
        }
        return OTHER_BASE_TYPE_MAP.containsKey(qName);
    }

    /**
     * 获取Java 基本类型的默认值
     *
     * @param typeName 类型
     * @return 默认值
     */
    public static String getJavaBaseTypeDefaultValStr(String typeName) {
        if (typeName == null) {
            return null;
        }
        TypeInfo typeInfo = JAVA_BASE_TYPE_MAP.get(typeName.toLowerCase());
        if (typeInfo == null) {
            return null;
        }
        return typeInfo.getDefaultValStr();
    }

    /**
     * 根据全限定名获取默认值字符串
     *
     * @param qName 全限定名
     * @return 默认值字符串
     */
    public static String getDefaultValStrByQname(String qName) {
        TypeInfo typeInfo = getTypeInfoByQname(qName);
        if (typeInfo == null) {
            return null;
        }
        return typeInfo.getDefaultValStr();
    }

    /**
     * 根据全限定名获取默认值对应的导入包信息
     *
     * @param qName 全限定名
     * @return 导入包信息
     */
    public static String getDefaultValImportByQname(String qName) {
        TypeInfo typeInfo = getTypeInfoByQname(qName);
        if (typeInfo == null) {
            return null;
        }
        return typeInfo.getImportStr();
    }

    /**
     * 获取两种 base 类型的默认值
     *
     * @param psiType     psiType
     * @param commentInfo 参数
     * @return 默认值
     */
    public static Object getBaseDefaultVal(PsiType psiType, CommentInfo commentInfo) {
        if (psiType == null) {
            return null;
        }
        String typeName = psiType.getPresentableText();
        String qName = psiType.getCanonicalText();
        TypeInfo typeInfo = JAVA_BASE_TYPE_MAP.get(typeName.toLowerCase());
        if (typeInfo == null) {
            typeInfo = OTHER_BASE_TYPE_MAP.get(qName);
        }
        if (typeInfo != null) {
            return typeInfo.getDefaultValGetFn().apply(commentInfo);
        }
        return null;
    }

    /**
     * 获取特定类型的默认值
     *
     * @param qName       特定类型全限定名
     * @param commentInfo 参数
     * @return 默认值
     */
    public static Object getInterfaceDefaultVal(String qName, CommentInfo commentInfo) {
        TypeInfo typeInfo = OTHER_INTERFACE_MAP.get(qName);
        if (typeInfo != null) {
            return typeInfo.getDefaultValGetFn().apply(commentInfo);
        }
        return null;
    }


    private static TypeInfo getTypeInfoByQname(String qName) {
        TypeInfo typeInfo = OTHER_BASE_TYPE_MAP.get(qName);
        if (typeInfo == null) {
            typeInfo = OTHER_INTERFACE_MAP.get(qName);
        }
        return typeInfo;
    }

    private static boolean typeIsObject(String typeName) {
        return "Object".equals(typeName);
    }

    private static boolean notUsingRandom() {
        return PluginSettingHelper.getConfigItem(PluginSettingEnum.DEFAULT_NOT_RANDOM, false);
    }

    private static String randomDate(CommentInfo commentInfo) {
        Date now = new Date();
        String pattern = commentInfo.getSingleStr(MoreCommentTagEnum.JSON_FORMAT.getTag(), "yyyy-MM-dd'T'HH:mm:ss.SSS+0000");
        pattern = commentInfo.getSingleStr(MoreCommentTagEnum.DATE_FORMAT.getTag(), pattern);
        now.setTime(System.currentTimeMillis() + RandomUtils.nextLong(0, 86400000));
        if (notUsingRandom()) {
            now.setTime(1338182040520L);
        }
        return DateFormatUtils.format(now, pattern);
    }

    private static String randomString(CommentInfo commentInfo) {
        String fieldDesc = commentInfo.getValue("");
        Boolean random = commentInfo.getSingleBool(MoreCommentTagEnum.EXAMPLE_RANDOM.getTag(), false);
        Boolean guid = commentInfo.getSingleBool(MoreCommentTagEnum.EXAMPLE_GUID.getTag(), false);
        if (guid) {
            if (notUsingRandom()) {
                return "98100F81-C8D8-45F8-9658-F31F5DC693C2";
            }
            return UUID.randomUUID().toString().toUpperCase();
        }
        // 强指定随机或字段无任何描述时, 随机生成
        //   否则使用描述加随机数字后缀组成示例值
        if (random || StringUtils.isBlank(fieldDesc)) {
            if (notUsingRandom()) {
                return "HelloWorld";
            }
            int length = RandomUtils.nextInt(5, 20);
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                stringBuilder.append(randomChar(false));
            }
            return stringBuilder.toString();
        } else {
            if (fieldDesc.contains(CommonConst.BREAK_LINE)) {
                fieldDesc = fieldDesc.substring(0, fieldDesc.indexOf(CommonConst.BREAK_LINE));
            }
            if (notUsingRandom()) {
                return fieldDesc;
            }
            return fieldDesc + RandomUtils.nextInt(1, 128);
        }
    }

    private static long randomLong() {
        if (notUsingRandom()) {
            return 10L;
        }
        return RandomUtils.nextLong(10, 1000);
    }

    private static float randomFloat() {
        if (notUsingRandom()) {
            return 10f;
        }
        return RandomUtils.nextFloat(10, 100);
    }

    private static int randomInt() {
        if (notUsingRandom()) {
            return 1;
        }
        return RandomUtils.nextInt(1, 1024);
    }

    private static short randomShort() {
        if (notUsingRandom()) {
            return (short) 1;
        }
        return (short) RandomUtils.nextInt(1, 100);
    }

    private static byte randomByte() {
        if (notUsingRandom()) {
            return (byte) 1;
        }
        return RandomUtils.nextBytes(1)[0];
    }

    private static boolean randomBoolean() {
        if (notUsingRandom()) {
            return false;
        }
        return RandomUtils.nextBoolean();
    }

    private static double randomDouble() {
        if (notUsingRandom()) {
            return 50d;
        }
        return RandomUtils.nextDouble(50, 1000);
    }

    private static char randomChar() {
        if (notUsingRandom()) {
            return 'Q';
        }
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
