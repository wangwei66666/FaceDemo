package com.linkever.jni.study.facedemo.face;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.linkever.jni.study.facedemo.R;
import com.linkever.jni.study.facedemo.utils.StreamUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 表情工具类
 * Author:      WW
 * Date:        2018/3/9 10:01
 * Description: This is Face
 */

public class Face {

    /**
     * 全局的表情映射，ArrayMap在低数量级的时候比HashMap又更优秀的内存管理，且更加轻量级，响应速度更快
     */
    private static final ArrayMap<String, Bean> FACE_MAP = new ArrayMap<>();
    private static List<FaceTab> FACE_TABS = null;

    /**
     * 初始化
     *
     * @param context
     */
    private static void init(Context context) {
        if (FACE_TABS == null) {
            synchronized (Face.class) {
                if (FACE_TABS == null) {
                    ArrayList<FaceTab> faceTabs = new ArrayList<>();
                    FaceTab tab = initAssetsFace(context);
                    if (tab != null) {
                        faceTabs.add(tab);
                    }
                    //获取zip表情盘

                    tab = initResourceFace(context);
                    if (tab != null) {
                        faceTabs.add(tab);
                    }
                    //init map
                    for (FaceTab faceTab : faceTabs) {
                        //加入map中，方便快速映射
                        faceTab.copyToMap(FACE_MAP);
                    }
                    //init list 不可变封装
                    FACE_TABS = Collections.unmodifiableList(faceTabs);
                }
            }
        }
    }

    /**
     * 从face-t.zip中解析表情
     *
     * @param context
     * @return
     */
    private static FaceTab initAssetsFace(Context context) {
        String faceAsset = "face-t.zip";
        //将.zip复制到文件缓存目录/data/data/包名/files/face/ft/*
        String faceCacheDir = String.format("%s/face/tf", context.getFilesDir());
        File faceFolder = new File(faceCacheDir);
        if (!faceFolder.exists()) {
            //不存在进行初始化
            if (faceFolder.mkdirs()) {
                try {
                    //输入流，打开表情文件
                    InputStream inputStream = context.getAssets().open(faceAsset);
                    //存储文件
                    File faceSource = new File(faceFolder, "source.zip");
                    //copy操作
                    StreamUtil.copy(inputStream, faceSource);
                    //解压
                    unZipFile(faceSource, faceFolder);
                    //清理文件
                    StreamUtil.delete(faceSource.getAbsolutePath());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //info.json 找到文件
        File infoFile = new File(faceCacheDir, "info.json");
        //Gson解析
        Gson gson = new Gson();
        JsonReader reader;
        try {
            reader = gson.newJsonReader(new FileReader(infoFile));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //文件找不到
            return null;
        }
        //文件找到，文件解析
        FaceTab tab = gson.fromJson(reader, FaceTab.class);
        //相对路径转绝对路径
        for (Bean face : tab.faces) {
            face.preview = String.format("%s/%s", faceCacheDir, face.preview);
            face.source = String.format("%s/%s", faceCacheDir, face.source);
        }
        return tab;
    }

    /**
     * 解压到目录
     *
     * @param zipFile 解压文件
     * @param desDir  解压目录
     */
    private static void unZipFile(File zipFile, File desDir) throws IOException {
        final String folderPath = desDir.getAbsolutePath();
        //构建zipFile
        ZipFile zf = new ZipFile(zipFile);
        // 判断节点进行循环
        for (Enumeration<?> entries = zf.entries(); entries.hasMoreElements(); ) {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            // 过滤缓存的文件
            String name = entry.getName();
            if (name.startsWith("."))
                continue;
            // 输入流
            InputStream in = zf.getInputStream(entry);
            String str = folderPath + File.separator + name;
            // 防止名字错乱
            str = new String(str.getBytes("8859_1"), "GB2312");
            File desFile = new File(str);
            // 输出文件
            StreamUtil.copy(in, desFile);
        }
    }

    /**
     * 从drawable资源中加载数据并映射到对应的key
     *
     * @param context
     * @return
     */
    private static FaceTab initResourceFace(Context context) {
        final ArrayList<Bean> faces = new ArrayList<>();
        final Resources resources = context.getResources();
        String packageName = context.getApplicationInfo().packageName;
        for (int i = 1; i <= 142; i++) {
            String key = String.format(Locale.ENGLISH, "fb%03d", i);
            String resStr = String.format(Locale.ENGLISH, "face_base_%03d", i);
            //根据资源名称去拿对应的ID
            int resId = resources.getIdentifier(resStr, "drawable", packageName);
            if (resId == 0) {
                continue;
            }
            //添加资源
            faces.add(new Bean(key, resId));
        }
        if (faces.size() == 0) {
            return null;
        }
        return new FaceTab("Drawable表情", faces.get(0).preview, faces);
    }

    /**
     * 获取所有表情（盘）
     *
     * @param context 上下文
     * @return
     */
    public static List<FaceTab> all(@NonNull Context context) {
        init(context);
        return FACE_TABS;
    }

    /**
     * 输入框输入表情
     *
     * @param context  上下文对象
     * @param editable 是否可舒服
     * @param bean     输入的表情对象
     * @param size     输入表情占用的大小
     */
    public static void inputFace(@NonNull final Context context, final Editable editable,
                                 final Bean bean, final int size) {
        Glide.with(context)
                .load(bean.preview)
                .asBitmap()
                //bitmap显示，gilde会自动帮我们剪切大小
                .into(new SimpleTarget<Bitmap>(size, size) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                        //打上一个标签
                        Spannable spannable = new SpannableString(String.format("[%s]", bean.key));

                        //上下文、资源 、对齐方式：基线对齐
                        ImageSpan span = new ImageSpan(context, resource, ImageSpan.ALIGN_BASELINE);
                        //设置span，开始 0字节，结束 字符串总长度，状态：后面输入内容前后不关联
                        spannable.setSpan(span, 0, spannable.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                        editable.append(spannable);
                    }
                });
    }

    /**
     * 从spannable解析表情并替换显示
     *
     * @param target
     * @param spannable
     * @param size
     * @return
     */
    public static Spannable decode(@NonNull View target,
                                   final Spannable spannable, final int size) {
        //spanable为空返回null
        if (spannable == null) {
            return null;
        }
        //字符串为空返回null
        String str = spannable.toString();
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        final Context context = target.getContext();
        // 进行正则匹配[][][]
        Pattern pattern = Pattern.compile("(\\[[^\\[\\]:\\s\\n]+\\])");
        Matcher matcher = pattern.matcher(str);
        //匹配到触发find()
        while (matcher.find()) {
            //类似[ft23]
            String key = matcher.group();
            if (TextUtils.isEmpty(key)) {
                continue;
            }
            //key对应的bean ft001 --> [ft001]
            Bean bean = get(context, key.replace("[", "").replace("]", ""));
            if (bean == null) {
                continue;
            }
            final int start = matcher.start();
            final int end = matcher.end();
            //得到一个复写后的标示
            ImageSpan span = new FaceSpan(context, target, bean.preview, size);
            //设置标示
            spannable.setSpan(span, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        }
        return spannable;
    }

    /**
     * 重写ImageSpan表情标示
     */
    public static class FaceSpan extends ImageSpan {

        //自己真是绘制的draw able
        private Drawable mDrawable;
        private View mView;
        private int mSize;

        /**
         * 构造函数
         *
         * @param context 上下文
         * @param view    目标view，用于加载完成时刷新使用
         * @param source  加载目标
         * @param size    size
         */
        public FaceSpan(Context context, final View view, Object source, final int size) {
            //上下文 ，默认图片，对其方式（底部对其）
            //虽然设置了默认的表情，但并不显示，只是用于占位
            super(context, R.drawable.default_face, ALIGN_BOTTOM);
            mView = view;
            mSize = size;
            Glide.with(context)
                    .load(source)
                    .fitCenter()
                    //bitmap显示，gilde会自动帮我们剪切大小
                    .into(new SimpleTarget<GlideDrawable>(size, size) {
                        @Override
                        public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> glideAnimation) {
                            mDrawable = resource.getCurrent();
                            //获取自测量高宽
                            int width = mDrawable.getIntrinsicWidth();
                            int height = mDrawable.getIntrinsicHeight();
                            //设置到Bound进去
                            mDrawable.setBounds(0, 0, width > 0 ? width : size, height > 0 ? height : size);
                            //通知刷新
                            view.invalidate();
                        }
                    });
        }

        /**
         * 复写getDrawable方法（测量大小时会调用getSize()方法时，会走getDrawable方法）
         *
         * @return
         */
        @Override
        public Drawable getDrawable() {
            //返回我的Drawable，当然这里返回有可能时null,所以复写draw方法（只有不为空时绘制）
            return mDrawable;
        }

        /**
         * 拿大小
         * @param paint
         * @param text
         * @param start
         * @param end
         * @param fm
         * @return
         */
        @Override
        public int getSize(Paint paint, CharSequence text, int start, int end, Paint.FontMetricsInt fm) {
            // 走我们自己的逻辑，进行测量
            Rect rect = mDrawable != null ? mDrawable.getBounds() :
                    new Rect(0, 0, mSize, mSize);

            if (fm != null) {
                fm.ascent = -rect.bottom;
                fm.descent = 0;

                fm.top = fm.ascent;
                fm.bottom = 0;
            }

            return rect.right;
        }

        @Override
        public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
            //增加判断,mDrawable不等于null时进行父类绘制
            if (mDrawable != null) {
                super.draw(canvas, text, start, end, x, top, y, bottom, paint);
            }

        }

    }

    /**
     * 得到key对应额Bean
     *
     * @param context
     * @param key     ft001
     * @return
     */
    public static Bean get(Context context, String key) {
        //初始化
        init(context);
        //map能匹配到key的存在
        if (FACE_MAP.containsKey(key)) {
            return FACE_MAP.get(key);
        }
        return null;
    }

    /**
     * 每一个表情盘，含有很多表情
     */
    public static class FaceTab {
        /**
         * 表情list，命名参考info文件
         */
        public List<Bean> faces = new ArrayList<>();
        /**
         * 表情对应的名字
         */
        public String name;
        /**
         * 表情预览图，zip中是预览图，drawable下面是int值
         */
        public Object preview;

        FaceTab(String name, Object preview, List<Bean> faces) {
            this.faces = faces;
            this.name = name;
            this.preview = preview;
        }

        //添加到Map
        void copyToMap(ArrayMap<String, Bean> faceMap) {
            for (Bean face : faces) {
                faceMap.put(face.key, face);
            }
        }
    }

    /**
     * 每一个表情
     */
    public static class Bean {
        public String key;
        public String desc;
        /**
         * 原始地址或预览地址可能是drawable下面的int值也可能是zip中的，所以定义为object
         */
        public Object source;
        public Object preview;

        Bean(String key, int preview) {
            this.key = key;
            this.source = preview;
            this.preview = preview;
        }
    }
}
