package com.szmurphy.richtext;

import static android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.TextAppearanceSpan;
import android.text.style.UnderlineSpan;

import org.xml.sax.Attributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class ExtendTagHandler implements HtmlTagHandler.TagHandler{
    private int startIndex;
    private int endIndex;

    private final List<Integer> fontSizeStartIndexList = new ArrayList<>();
    private int fontListIndex = 0;
    private final Map<Integer, Integer> fontSizeProcessIndexMap = new HashMap<>();



    private final List<Integer> colorStartIndexList = new ArrayList<>();
    private int colorListIndex = 0;
    private final Map<Integer, Integer> colorProcessIndexMap = new HashMap<>();

    private final List<Integer> underlineStartIndexList = new ArrayList<>();
    private int underlineListIndex = 0;
    private final Map<Integer, Integer> underlineProcessIndexMap = new HashMap<>();
    private Context context;
    private ColorStateList mOriginColors;

    private final Stack<Map<String, String>> mStack = new Stack();

    public ExtendTagHandler(Context context, ColorStateList originColors) {
        this.context = context;
        startIndex = 0;
        endIndex = 0;
        mOriginColors = originColors;
    }

    @Override
    public void handleTag(boolean open, String tag, Editable output, Attributes attrs) {

        try {
            if (open) {
                //开标签，output是空（sax还没读到），attrs有值
                if (tag.equalsIgnoreCase("span")
                        || tag.equalsIgnoreCase("strike")
                        || tag.equalsIgnoreCase("i")
                        || tag.equalsIgnoreCase("u")
                        || tag.equalsIgnoreCase("font")) {
                    parseStyle(attrs, output.length());
                }
                startIndex = output.length();
            } else {
                //闭标签，output有值了，attrs没值
                endIndex = output.length();
                if (tag.equalsIgnoreCase("span")) {
                    Map<String, String> attrMap = mStack.peek();
                    setForegroundColor(output,attrMap);
                    mStack.pop();
                } else if (tag.equalsIgnoreCase("strike")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    output.setSpan(new StrikethroughSpan(), startIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE);
                    mStack.pop();
                } else if (tag.equalsIgnoreCase("i")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    mStack.pop();
                } else if (tag.equalsIgnoreCase("u")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    mStack.pop();
                } else if (tag.equalsIgnoreCase("a")) {
                    output.setSpan(new UnderlineSpan(), startIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE);
                    reductionFontColor(startIndex,endIndex,output);
                    mStack.pop();
                } else if (tag.equalsIgnoreCase("font")) {
                    Map<String, String> attrMap = mStack.peek();
                    setBgColor(output,attrMap);
                    mStack.pop();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseStyle(Attributes attrs, int startIndex){
        Map<String, String> attrMap = new HashMap<>();
        for (int i = 0; i < attrs.getLength(); i++) {
            if (attrs.getLocalName(i).equals("style")) {
                String style = attrs.getValue(i);
                String[] attrArray = style.split(";");
                for (String attr : attrArray) {
                    String[] keyValueArray = attr.split(":");
                    if (keyValueArray.length == 2) {
                        if(keyValueArray[0].trim().equals("font-size")) {
                            fontSizeStartIndexList.add(startIndex);
                            fontListIndex = fontSizeStartIndexList.size()-1;
//                                LogUtil.d("XXX font-size,startIndex:"+startIndex+",fontListIndex:"+fontListIndex);
                        } else if(keyValueArray[0].trim().equals("color")) {
                            colorStartIndexList.add(startIndex);
                            colorListIndex = colorStartIndexList.size()-1;
                        } else if(keyValueArray[0].trim().equals("text-decoration")) {
                            underlineStartIndexList.add(startIndex);
                            underlineListIndex = underlineStartIndexList.size()-1;
                        }
                        // 记住要去除前后空格
                        attrMap.put(keyValueArray[0].trim(), keyValueArray[1].trim());
//                            LogUtil.d("----key:"+keyValueArray[0].trim()+",value:"+keyValueArray[1].trim());
                    }
                }

            }
        }
        mStack.push(attrMap);
    }

    private int getNextIndex(int currentIndex, Map<Integer, Integer> processIndexMap) {
        while(currentIndex > 0 && processIndexMap.containsKey(currentIndex)) {
            currentIndex--;
        }
        return currentIndex;
    }

    private Map<Integer, Integer> getIndexPair(int currentIndex, List<Integer> startIndexList, Map<Integer, Integer> processIndexMap) {
        Map<Integer, Integer> returnMap = new HashMap<>();
        int indexOffset = 0;
        int tmpStartIndex = startIndexList.get(currentIndex+indexOffset);
        int tmpEndIndex = endIndex;
        if(currentIndex < startIndexList.size()-1) {
            tmpEndIndex = startIndexList.get(currentIndex + 1);
        }
        if(tmpEndIndex > tmpStartIndex) {
            returnMap.put(tmpStartIndex,tmpEndIndex);
        }
        indexOffset++;
        while(tmpEndIndex < endIndex && processIndexMap.containsKey(currentIndex+indexOffset)) {
            tmpStartIndex = processIndexMap.get(currentIndex+indexOffset);
            if(currentIndex+indexOffset+1 <= startIndexList.size()-1) {
                int offset = 1;
//                LogUtil.d("tmpStartIndex:"+tmpStartIndex+",tmpEndIndex:"+startIndexList.get(currentIndex+indexOffset+offset)+",currentIndex:"+currentIndex+",indexOffset:"+indexOffset+",offset:"+offset);
                while(currentIndex+indexOffset+offset <= startIndexList.size()-1 && startIndexList.get(currentIndex+indexOffset+offset) < tmpStartIndex) {
                    offset++;
                }
                tmpEndIndex = startIndexList.get(currentIndex + indexOffset + offset);
                indexOffset += offset;
            } else {
                tmpEndIndex = endIndex;
                indexOffset ++;
            }
//            LogUtil.d("tmpStartIndex:"+tmpStartIndex+",tmpEndIndex:"+tmpEndIndex+",currentIndex:"+currentIndex+",indexOffset:"+indexOffset);
            if(tmpEndIndex > tmpStartIndex) {
                returnMap.put(tmpStartIndex,tmpEndIndex);
            }
        }
        return returnMap;
    }


    private void setForegroundColor(Editable output, Map<String, String> attrMap){
        String color = attrMap.get("color");
        String fontSize = attrMap.get("font-size");
        if(!TextUtils.isEmpty(fontSize) && fontSize.contains("px")) {
            fontSize = fontSize.split("px")[0];
        }
        String underline = attrMap.get("text-decoration");
        if (!TextUtils.isEmpty(color)) {
            Map<Integer, Integer> indexMap = getIndexPair(colorListIndex, colorStartIndexList, colorProcessIndexMap);
            for (Map.Entry<Integer, Integer> entry:indexMap.entrySet()) {
//                LogUtil.d("color:"+color+", startIndex:"+entry.getKey()+",endIndex:"+entry.getValue()+",colorListIndex:"+ colorListIndex +",endIndex:"+endIndex);
                if (color.startsWith("@")) {
                    Resources res = Resources.getSystem();
                    String name = color.substring(1);
                    int colorRes = res.getIdentifier(name, "color", "android");
                    if (colorRes != 0) {
                        output.setSpan(new ForegroundColorSpan(colorRes), entry.getKey(), entry.getValue(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    }
                } else {
                    try {
                        output.setSpan(new ForegroundColorSpan(Color.parseColor(color)),entry.getKey(), entry.getValue(), SPAN_EXCLUSIVE_EXCLUSIVE);
                    } catch (Exception e) {
                        e.printStackTrace();
                        reductionFontColor(startIndex,endIndex,output);
                    }
                }
            }
            colorProcessIndexMap.put(colorListIndex, endIndex);
            colorListIndex = getNextIndex(colorListIndex, colorProcessIndexMap);
        }
        setBgColor(output,attrMap);

        if(!TextUtils.isEmpty(fontSize)){
            Map<Integer, Integer> indexMap = getIndexPair(fontListIndex, fontSizeStartIndexList, fontSizeProcessIndexMap);
            for (Map.Entry<Integer, Integer> entry:indexMap.entrySet()) {
//                LogUtil.d("fontSize:"+fontSize+", startIndex:"+entry.getKey()+",endIndex:"+entry.getValue()+",fontListIndex:"+ fontListIndex +",endIndex:"+endIndex);
                output.setSpan(new AbsoluteSizeSpan(Integer.parseInt(fontSize)), entry.getKey(), entry.getValue(), SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            fontSizeProcessIndexMap.put(fontListIndex, endIndex);
            fontListIndex = getNextIndex(fontListIndex, fontSizeProcessIndexMap);

        }

        if (!TextUtils.isEmpty(underline)) {
            Map<Integer, Integer> indexMap = getIndexPair(underlineListIndex, underlineStartIndexList, underlineProcessIndexMap);
            for (Map.Entry<Integer, Integer> entry:indexMap.entrySet()) {
//                LogUtil.d("underline:"+underline+", startIndex:"+entry.getKey()+",endIndex:"+entry.getValue()+",underlineListIndex:"+ underlineListIndex +",endIndex:"+endIndex);
                output.setSpan(new UnderlineSpan(), entry.getKey(), entry.getValue(), SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            underlineProcessIndexMap.put(underlineListIndex, endIndex);
            underlineListIndex = getNextIndex(underlineListIndex, underlineProcessIndexMap);
        }
    }


    private void setBgColor(Editable output, Map<String, String> attrMap){
        if(attrMap!=null){
            String background = attrMap.get("background-color");
            if(background!=null){
                String color1 = background.substring("rgb(".length(),background.indexOf(","));
                background = background.substring(background.indexOf(color1)+color1.length()+1);
                String color2 = background.substring(0,background.indexOf(","));
                background = background.substring(background.indexOf(color2)+color2.length()+1);
                String color3 = background.substring(0,background.indexOf(")"));
                output.setSpan(new BackgroundColorSpan(Color.rgb(Integer.parseInt(color1.trim()), Integer.parseInt(color2.trim()), Integer.parseInt(color3.trim()))), startIndex, endIndex, SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * 还原为原来的颜色
     * @param startIndex 开始索引
     * @param stopIndex 结束索引
     * @param editable 编辑器
     */
    private void reductionFontColor(int startIndex,int stopIndex,Editable editable){
        if (null != mOriginColors){
            editable.setSpan(new TextAppearanceSpan(null, 0, 0, mOriginColors, null),
                    startIndex, stopIndex,
                    SPAN_EXCLUSIVE_EXCLUSIVE);
        }else {
            editable.setSpan(new ForegroundColorSpan(0xff2b2b2b), startIndex, stopIndex, SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }
}
