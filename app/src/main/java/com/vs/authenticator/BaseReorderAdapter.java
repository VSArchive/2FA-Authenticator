package com.vs.authenticator;

import android.content.ClipData;
import android.view.DragEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public abstract class BaseReorderAdapter extends BaseAdapter {
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            int type = getItemViewType(position);
            convertView = createView(parent, type);

            //unavoidable generic type problems -> Reference<View>
            convertView.setOnDragListener((dstView, event) -> {
                Reference<View> ref = (Reference<View>) event.getLocalState();
                final View srcView = ref.reference;

                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_ENTERED:
                        srcView.setVisibility(View.VISIBLE);
                        dstView.setVisibility(View.INVISIBLE);

                        move(((Integer) srcView.getTag(R.id.reorder_key)),
                                ((Integer) dstView.getTag(R.id.reorder_key)));
                        ref.reference = dstView;
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        srcView.post(() -> srcView.setVisibility(View.VISIBLE));
                        break;
                }

                return true;
            });

            convertView.setOnLongClickListener(view -> {
                // Force a reset of any states
                notifyDataSetChanged();

                // Start the drag on the main loop to allow
                // the above state reset to settle.
                view.post(() -> {
                    ClipData data = ClipData.newPlainText("", "");
                    DragShadowBuilder sb = new DragShadowBuilder(view);
                    view.startDrag(data, sb, new Reference<>(view), 0);
                });

                return true;
            });
        }

        convertView.setTag(R.id.reorder_key, position);
        bindView(convertView, position);
        return convertView;
    }

    protected abstract void move(int fromPosition, int toPosition);

    protected abstract void bindView(View view, int position);

    protected abstract View createView(ViewGroup parent, int type);

    private static class Reference<T> {
        T reference;

        public Reference(T t) {
            reference = t;
        }
    }
}
