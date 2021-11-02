package com.vs.authenticator

import android.content.ClipData
import android.view.DragEvent
import android.view.View
import android.view.View.DragShadowBuilder
import android.view.ViewGroup
import android.widget.BaseAdapter

abstract class BaseReorderAdapter : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertViewSave = convertView
        if (convertViewSave == null) {
            val type = getItemViewType(position)
            convertViewSave = createView(parent, type)

            //unavoidable generic type problems -> Reference<View>
            convertViewSave.setOnDragListener { dstView: View, event: DragEvent ->
                val ref = event.localState as Reference<View>
                val srcView = ref.reference
                when (event.action) {
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        srcView.visibility = View.VISIBLE
                        dstView.visibility = View.INVISIBLE
                        move(
                            srcView.getTag(R.id.reorder_key) as Int,
                            dstView.getTag(R.id.reorder_key) as Int
                        )
                        ref.reference = dstView
                    }
                    DragEvent.ACTION_DRAG_ENDED -> srcView.post {
                        srcView.visibility = View.VISIBLE
                    }
                }
                true
            }
            convertViewSave.setOnLongClickListener { view: View ->
                // Force a reset of any states
                notifyDataSetChanged()

                // Start the drag on the main loop to allow
                // the above state reset to settle.
                view.post {
                    val data = ClipData.newPlainText("", "")
                    val sb = DragShadowBuilder(view)
                    view.startDragAndDrop(data, sb, Reference(view), 0)
                }
                true
            }
        }
        convertViewSave.setTag(R.id.reorder_key, position)
        bindView(convertViewSave, position)
        return convertViewSave
    }

    protected abstract fun move(fromPosition: Int, toPosition: Int)
    protected abstract fun bindView(view: View?, position: Int)
    protected abstract fun createView(parent: ViewGroup?, type: Int): View
    private class Reference<T>(var reference: T)
}