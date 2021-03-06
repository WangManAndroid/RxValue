package com.cyss.rxvalue;

import android.content.Context;
import android.view.View;

import com.cyss.rxvalue.listener.OnDataComplete;
import com.cyss.rxvalue.listener.OnDataError;
import com.cyss.rxvalue.listener.OnFillComplete;
import com.cyss.rxvalue.listener.OnFillError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by chenyang on 2017/2/10.
 */

public class RxValueBuilder<T, E extends RxValueBuilder> implements Serializable {
    protected static final int DEFAULT_LAYOUT = 1;
    protected Context context;
    //填充的数据
    protected T fillObj;
    //layout id前缀
    protected String prefix;
    //layout id后缀
    protected String suffix;
    //需要填充的set view集合，若为空则默认填充全部支持类型
    protected Set<Class<? extends View>> fillViewType = new HashSet<>();
    //java bean 名称转换注解信息集合
    protected Map<String, String> objIdNameMap = new HashMap<>();
    //使用convertKey,临时存储
    protected Map<String, String> objIdNameTempMap = new HashMap<>();
    //java bean date转换注解信息集合
    protected Map<String, String> objDateMap = new HashMap<>();
    protected Map<Class<? extends View>, CustomFillAction> customFillActionMap = new HashMap<>();
    protected OnDataComplete<T> dataComplete;
    protected OnDataError dataError;
    protected OnFillComplete fillComplete;
    protected OnFillError fillError;
    protected int layoutId = DEFAULT_LAYOUT;

    protected static Map<String, Integer> layoutMap;
    protected static Map<Integer, String> layoutResMap;
    protected static Map<String,Integer> idsMap;
    protected static Map<Integer, String> idsResMap;
    protected static Map<Class<? extends View>, CustomFillAction> globalCustomFillActionMap = new ConcurrentHashMap<>();

    public static CustomFillAction cloneCustomFillAction(CustomFillAction obj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(bo);
        oo.writeObject(obj);

        ByteArrayInputStream bi=new ByteArrayInputStream(bo.toByteArray());
        ObjectInputStream oi = new ObjectInputStream(bi);
        return (CustomFillAction) oi.readObject();
    }


    /**
     * 添加view id前缀
     * @param prefix
     * @return
     */
    public E withPrefix(String prefix) {
        this.prefix = prefix;
        return (E) this;
    }

    /**
     * 添加view id后缀
     * @param suffix
     * @return
     */
    public E withSuffix(String suffix) {
        this.suffix = suffix;
        return (E) this;
    }

    /**
     * 限制填充类型
     * @param clazz 填充类型
     * @return
     */
    public E viewType(Class<? extends View> clazz) {
        fillViewType.add(clazz);
        return (E) this;
    }

    /**
     * 限制填充类型
     * @param viewSets 填充类型集合
     * @return
     */
    public E viewSets(Set<Class<? extends View>> viewSets) {
        fillViewType.addAll(viewSets);
        return (E) this;
    }

    public E clearViewType() {
        fillViewType.clear();
        return (E) this;
    }

    /**
     * 注册自定义填充行为
     * @param clazz   填充view类型
     * @param action  填充行为
     * @return
     */
    public E registerAction(Class<? extends View> clazz, CustomFillAction action) {
        customFillActionMap.put(clazz, action);
        return (E) this;
    }

    /**
     * 注册自定义填充行为
     * @param actions
     * @return
     */
    public E registerActions(Map<Class<? extends View>, CustomFillAction> actions) {
        customFillActionMap.putAll(actions);
        return (E) this;
    }

    /**
     * 填充完成回调
     * @param complete
     * @return
     */
    public E withFillComplete(OnFillComplete complete) {
        this.fillComplete = complete;
        return (E) this;
    }

    /**
     * 获取数据完成回调
     * @param complete
     * @return
     */
    public E withDataComplete(OnDataComplete<T> complete) {
        this.dataComplete = complete;
        return (E) this;
    }

    /**
     * 填充错误回调
     * @param error
     * @return
     */
    public E withFillError(OnFillError error) {
        this.fillError = error;
        return (E) this;
    }

    /**
     * 填充错误回调
     * @param error
     * @return
     */
    public E withDataError(OnDataError error) {
        this.dataError = error;
        return (E) this;
    }

    /**
     * 添加需要转换的参数key，多用于fillObj为Map时。Java Bean可使用@IdName注解
     * @param paramName   参数的名称
     * @param layoutName  xml中id名称
     * @return
     */
    public E convertKey(String paramName, String layoutName) {
        objIdNameTempMap.put(layoutName, paramName);
        return (E) this;
    }

    public E removeConvertKey(String paramName) {
        objIdNameTempMap.remove(paramName);
        return (E) this;
    }

    /**
     * 设置layout id
     * @param layoutId
     * @return
     */
    public E layoutId(int layoutId) {
        this.layoutId = layoutId;
        if (fillObj != null) withFillObj(fillObj);
        return (E) this;
    }

    /**
     * 设置需要填充或获取的obj参数
     * @param obj
     * @return
     */
    public E withFillObj(T obj) {
        if (obj == null) return (E) this;
        this.fillObj = obj;
        return (E) this;
    }

    public E setBuilder(RxValueBuilder builder) {
        if (builder == null) return (E) this;
        withPrefix(builder.getPrefix());
        withSuffix(builder.getSuffix());
        fillViewType = builder.getFillViewType();
        objIdNameTempMap = builder.getObjIdNameTempMap();

        customFillActionMap.clear();
        customFillActionMap.putAll(globalCustomFillActionMap);
        for (Map.Entry<Class<? extends View>, CustomFillAction> item : globalCustomFillActionMap.entrySet()) {
            try {
                customFillActionMap.put(item.getKey(), cloneCustomFillAction(item.getValue()));
            } catch (ClassNotFoundException e) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        customFillActionMap.putAll(builder.getCustomFillActionMap());

        layoutId(builder.getLayoutId());
        withDataComplete(builder.getDataComplete());
        withDataError(builder.getDataError());
        withFillComplete(builder.getFillComplete());
        withFillError(builder.getFillError());
        return (E) this;
    }

    public Context getContext() {
        return context;
    }

    public String getPrefix() {
        return prefix;
    }

    public T getFillObj() {
        return fillObj;
    }

    public String getSuffix() {
        return suffix;
    }

    public Set<Class<? extends View>> getFillViewType() {
        return fillViewType;
    }

    public Map<String, String> getObjIdNameTempMap() {
        return objIdNameTempMap;
    }

    public Map<String, String> getObjDateMap() {
        return objDateMap;
    }

    public Map<Class<? extends View>, CustomFillAction> getCustomFillActionMap() {
        return customFillActionMap;
    }

    public CustomFillAction getFillAction(Class<? extends View> clazz) {
        return customFillActionMap.get(clazz);
    }

    public int getLayoutId() {
        return layoutId;
    }

    public OnDataComplete<T> getDataComplete() {
        return dataComplete;
    }

    public OnDataError getDataError() {
        return dataError;
    }

    public OnFillComplete getFillComplete() {
        return fillComplete;
    }

    public OnFillError getFillError() {
        return fillError;
    }
}
