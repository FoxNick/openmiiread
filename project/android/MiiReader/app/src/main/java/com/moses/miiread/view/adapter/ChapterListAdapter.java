//Copyright (c) 2017. origin. All rights reserved.
package com.moses.miiread.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.moses.miiread.MApplication;
import com.moses.miiread.R;
import com.moses.miiread.base.observer.SimpleObserver;
import com.moses.miiread.bean.BookShelfBean;
import com.moses.miiread.bean.ChapterListBean;
import com.moses.miiread.utils.theme.ThemeStore;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class ChapterListAdapter extends RecyclerView.Adapter<ChapterListAdapter.ThisViewHolder> {

    private BookShelfBean bookShelfBean;
    private OnItemClickListener itemClickListener;
    private List<ChapterListBean> chapterListBeans = new ArrayList<>();
    private int index = 0;
    private boolean isSearch = false;
    private int normalColor;
    private int highlightColor;
    private int cachedColor;

    public ChapterListAdapter(BookShelfBean bookShelfBean, @NonNull OnItemClickListener itemClickListener) {
        this.bookShelfBean = bookShelfBean;
        this.itemClickListener = itemClickListener;
        highlightColor = MApplication.getInstance().getResources().getColor(R.color.colorControlActivated);
        cachedColor = MApplication.getInstance().getResources().getColor(R.color.tv_text_summary);
    }

    public void upChapter(int index) {
        if (bookShelfBean.getChapterListSize() > index) {
            notifyItemChanged(index, 0);
        }
    }

    public void search(final String key) {
        chapterListBeans.clear();
        if (Objects.equals(key, "")) {
            isSearch = false;
            notifyDataSetChanged();
        } else {
            Observable.create((ObservableOnSubscribe<Boolean>) emitter -> {
                for (ChapterListBean chapterListBean : bookShelfBean.getChapterList()) {
                    if (chapterListBean.getDurChapterName().contains(key)) {
                        chapterListBeans.add(chapterListBean);
                    }
                }
                emitter.onNext(true);
                emitter.onComplete();
            }).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SimpleObserver<Boolean>() {
                        @Override
                        public void onNext(Boolean aBoolean) {
                            isSearch = true;
                            notifyDataSetChanged();
                        }

                        @Override
                        public void onError(Throwable e) {

                        }
                    });
        }
    }

    @NonNull
    @Override
    public ThisViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        normalColor = ThemeStore.textColorSecondary(parent.getContext());
        return new ThisViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chapter_list, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, final int position) {

    }

    @Override
    public void onBindViewHolder(@NonNull ThisViewHolder holder, int position, @NonNull List<Object> payloads) {
        int realPosition = holder.getLayoutPosition();
        if (realPosition == getItemCount() - 1) {
            holder.line.setVisibility(View.GONE);
        } else {
            holder.line.setVisibility(View.VISIBLE);
        }
        if (payloads.size() > 0) {
            holder.tvName.setSelected(true);
            holder.tvName.getPaint().setFakeBoldText(true);
            return;
        }
        ChapterListBean chapterListBean = isSearch ? chapterListBeans.get(realPosition) : bookShelfBean.getChapter(realPosition);
        holder.tvName.setText(chapterListBean.getDurChapterName());
        if (Objects.equals(bookShelfBean.getTag(), BookShelfBean.LOCAL_TAG) || chapterListBean.getHasCache(bookShelfBean.getBookInfoBean())) {
            holder.tvName.setSelected(true);
            holder.tvName.getPaint().setFakeBoldText(true);
            holder.tvName.setTextColor(cachedColor);
        } else {
            holder.tvName.setSelected(false);
            holder.tvName.getPaint().setFakeBoldText(false);
            holder.tvName.setTextColor(normalColor);
        }
        if (chapterListBean.getDurChapterIndex() == index)
            holder.tvName.setTextColor(highlightColor);

        holder.llName.setOnClickListener(v -> {
            setIndex(realPosition);
            itemClickListener.itemClick(chapterListBean.getDurChapterIndex(), 0);
        });
    }

    @Override
    public int getItemCount() {
        if (bookShelfBean == null)
            return 0;
        else {
            if (isSearch) {
                return chapterListBeans.size();
            }
            return bookShelfBean.getChapterListSize();
        }
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
        notifyItemChanged(this.index, 0);
    }

    static class ThisViewHolder extends RecyclerView.ViewHolder {
        private TextView tvName;
        private View line;
        private View llName;

        ThisViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_name);
            line = itemView.findViewById(R.id.v_line);
            llName = itemView.findViewById(R.id.ll_name);
        }
    }

    public interface OnItemClickListener {
        void itemClick(int index, int page);
    }
}
