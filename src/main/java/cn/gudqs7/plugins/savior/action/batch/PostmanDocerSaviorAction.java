package cn.gudqs7.plugins.savior.action.batch;

import cn.gudqs7.plugins.common.base.action.AbstractBatchDocerSavior;
import cn.gudqs7.plugins.common.consts.MapKeyConstant;
import cn.gudqs7.plugins.common.enums.PluginSettingEnum;
import cn.gudqs7.plugins.common.pojo.resolver.CommentInfo;
import cn.gudqs7.plugins.common.util.JsonUtil;
import cn.gudqs7.plugins.common.util.PluginSettingHelper;
import cn.gudqs7.plugins.common.util.api.PostmanApiUtil;
import cn.gudqs7.plugins.common.util.file.FileUtil;
import cn.gudqs7.plugins.common.util.jetbrain.IdeaApplicationUtil;
import cn.gudqs7.plugins.common.util.structure.PsiClassUtil;
import cn.gudqs7.plugins.savior.savior.more.JavaToPostmanSavior;
import cn.gudqs7.plugins.savior.theme.ThemeHelper;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author wq
 */
public class PostmanDocerSaviorAction extends AbstractBatchDocerSavior {


    protected JavaToPostmanSavior postmanSavior;

    public PostmanDocerSaviorAction() {
        this.postmanSavior = new JavaToPostmanSavior(ThemeHelper.getRestfulTheme());
    }

    @Override
    protected void runLoopBefore(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String apiDocPath, Map<String, Object> otherMap) throws Throwable {
        Map<String, List<Map<String, Object>>> itemListMap = new LinkedHashMap<>(16);
        otherMap.put("itemListMap", itemListMap);
        otherMap.put("psiClassCount", 0);
    }

    @Override
    protected void runLoop(Project project, PsiClass psiClass0, AtomicBoolean hasCancelAtomic, CommentInfo commentInfo, String moduleName, String fileName, File parent, String fileParentDir, String fullFileName, Map<String, Object> otherMap, ProgressIndicator indicator, float fraction) {
        Map<String, List<Map<String, Object>>> itemListMap0 = getItemListMap0(otherMap);
        if (itemListMap0 == null) {
            return;
        }
        Integer psiClassCount = (Integer) otherMap.getOrDefault("psiClassCount", 0);
        otherMap.put("psiClassCount", psiClassCount + 1);

        indicator.setText2("处理中：" + moduleName + " - " + commentInfo.getItemName(psiClass0.getName()));
        indicator.setFraction(fraction);

        List<Map<String, Object>> itemList = itemListMap0.computeIfAbsent(moduleName, k -> new ArrayList<>(16));
        IdeaApplicationUtil.runReadAction(() -> {
            Map<String, Object> postmanItem = postmanSavior.generatePostmanItem(psiClass0, project);
            if (postmanItem != null) {
                Object o = postmanItem.remove(MapKeyConstant.HOST_PORT);
                if (o instanceof String) {
                    otherMap.put("hostAndPort", o);
                }
                otherMap.put("lastPostmanItem", postmanItem);
                itemList.add(postmanItem);
            }
        });
    }

    @Override
    protected void runLoopAfter(Project project, ProgressIndicator indicator, AtomicBoolean hasCancelAtomic, Set<PsiClass> finalPsiClassList, String docRootDirPath, Map<String, Object> otherMap) throws Throwable {
        Map<String, List<Map<String, Object>>> itemListMap0 = getItemListMap0(otherMap);
        if (itemListMap0 == null) {
            return;
        }
        String hostAndPort = otherMap.getOrDefault("hostAndPort", "").toString();

        String projectName = project.getName();
        String postmanName = projectName;
        boolean postmanOverride = true;
        String postmanKey = null;

        String name = PluginSettingHelper.getConfigItem(PluginSettingEnum.POSTMAN_NAME);
        if (StringUtils.isNotBlank(name)) {
            postmanName = name;
        }
        boolean postmanEnable = PluginSettingHelper.getConfigItem(PluginSettingEnum.POSTMAN_ENABLE, false);
        if (postmanEnable) {
            postmanOverride = PluginSettingHelper.getConfigItem(PluginSettingEnum.POSTMAN_OVERRIDE, true);
            if (!postmanOverride) {
                SimpleDateFormat format = new SimpleDateFormat("MM-dd_HH-mm");
                postmanName = postmanName + "-" + format.format(new Date());
            }
            postmanKey = PluginSettingHelper.getConfigItem(PluginSettingEnum.POSTMAN_KEY);
        }

        List<Map<String, Object>> itemList = new ArrayList<>(16);
        for (Map.Entry<String, List<Map<String, Object>>> entry : itemListMap0.entrySet()) {
            String key = entry.getKey();
            List<Map<String, Object>> list = entry.getValue();
            if (!CollectionUtils.isEmpty(list)) {
                Map<String, Object> item = new LinkedHashMap<>(4);
                item.put("name", key);
                item.put("item", list);

                itemList.add(item);
            }
        }

        Integer psiClassCount = (Integer) otherMap.getOrDefault("psiClassCount", 0);
        if (psiClassCount == 1) {
            Map<String, Object> lastPostmanItem = (Map<String, Object>) otherMap.get("lastPostmanItem");
            if (lastPostmanItem == null) {
                return;
            }
            postmanName = projectName + "-" + lastPostmanItem.getOrDefault("name", postmanName).toString();
            // 去除模块
            itemList.clear();

            // 含有则说明单个 Controller 但多个接口
            if (lastPostmanItem.containsKey("item")) {
                itemList.addAll((List<Map<String, Object>>) lastPostmanItem.get("item"));
            } else {
                itemList.add(lastPostmanItem);
            }
        }

        Map<String, Object> postmanObj = new LinkedHashMap<>(8);
        Map<String, Object> info = new LinkedHashMap<>(8);
        info.put("_postman_id", UUID.randomUUID().toString());
        info.put("name", postmanName);
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");
        Map<String, Object> variable = new LinkedHashMap<>(8);
        variable.put("key", projectName + "-url");
        variable.put("value", hostAndPort);

        postmanObj.put("info", info);
        postmanObj.put("item", itemList);
        postmanObj.put("variable", Collections.singletonList(variable));
        String json = JsonUtil.toJson(postmanObj);

        File parent = new File(docRootDirPath);
        FileUtil.writeStringToFile(json, parent, postmanName + ".postman_collection.json");

        if (postmanEnable && StringUtils.isNotBlank(postmanKey)) {
            Map<String, Object> collection = new HashMap<>(2);
            collection.put("collection", postmanObj);
            String collectionJson = JsonUtil.toJson(collection);
            String[] keyArray = postmanKey.split(",");
            for (String key : keyArray) {
                saveOrUpdateToPostman(postmanName, postmanOverride, key, collectionJson);
            }
        }
    }

    @Nullable
    private Map<String, List<Map<String, Object>>> getItemListMap0(Map<String, Object> otherMap) {
        Object itemListMap = otherMap.get("itemListMap");
        if (!(itemListMap instanceof Map)) {
            return null;
        }
        return (Map<String, List<Map<String, Object>>>) itemListMap;
    }

    private void saveOrUpdateToPostman(String postmanName, boolean postmanOverride, String postmanKey, String collectionJson) {
        if (postmanOverride) {
            PostmanApiUtil.updateCollection(postmanName, collectionJson, postmanKey);
        } else {
            PostmanApiUtil.addCollection(collectionJson, postmanKey);
        }
    }

    @Override
    protected String getDirPrefix() {
        return "postman";
    }

    @Override
    protected @NotNull String getFileExtension() {
        return ".postman_collection.json";
    }

    @Override
    protected boolean isNeedDealPsiClass(PsiClass psiClass, Project project) {
        return PsiClassUtil.isControllerOrFeign(psiClass);
    }

}
