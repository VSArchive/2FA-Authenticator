package com.vs.authenticator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.vs.authenticator.edit.DeleteActivity;
import com.vs.authenticator.edit.EditActivity;

import java.util.HashMap;
import java.util.Map;

public class TokenAdapter extends BaseReorderAdapter {
    private final TokenPersistence mTokenPersistence;
    private final LayoutInflater mLayoutInflater;
    private final ClipboardManager mClipMan;
    private final Map<String, TokenCode> mTokenCodes;

    public TokenAdapter(Context ctx) {
        mTokenPersistence = new TokenPersistence(ctx);
        mLayoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mClipMan = (ClipboardManager) ctx.getSystemService(Context.CLIPBOARD_SERVICE);
        mTokenCodes = new HashMap<>();
        registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                mTokenCodes.clear();
            }

            @Override
            public void onInvalidated() {
                mTokenCodes.clear();
            }
        });
    }

    @Override
    public int getCount() {
        return mTokenPersistence.length();
    }

    @Override
    public Token getItem(int position) {
        return mTokenPersistence.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    protected void move(int fromPosition, int toPosition) {
        mTokenPersistence.move(fromPosition, toPosition);
        notifyDataSetChanged();
    }

    @Override
    protected void bindView(View view, final int position) {
        final Context ctx = view.getContext();
        TokenLayout tl = (TokenLayout) view;
        Token token = getItem(position);

        tl.bind(token, R.menu.token, item -> {
            Intent i;

            switch (item.getItemId()) {
                case R.id.action_edit:
                    i = new Intent(ctx, EditActivity.class);
                    i.putExtra(EditActivity.EXTRA_POSITION, position);
                    ctx.startActivity(i);
                    break;

                case R.id.action_delete:
                    i = new Intent(ctx, DeleteActivity.class);
                    i.putExtra(DeleteActivity.EXTRA_POSITION, position);
                    ctx.startActivity(i);
                    break;
            }

            return true;
        });

        tl.setOnClickListener(v -> {
            TokenPersistence tp = new TokenPersistence(ctx);

            // Increment the token.
            Token token1 = tp.get(position);
            TokenCode codes = token1.generateCodes();
            //save token. Image wasn't changed here, so just save it in sync
            new TokenPersistence(ctx).save(token1);

            // Copy code to clipboard.
            mClipMan.setPrimaryClip(ClipData.newPlainText(null, codes.getCurrentCode()));
            Toast.makeText(v.getContext().getApplicationContext(),
                    R.string.code_copied,
                    Toast.LENGTH_SHORT).show();

            mTokenCodes.put(token1.getID(), codes);
            ((TokenLayout) v).start(token1.getType(), codes, true);
        });

        TokenCode tc = mTokenCodes.get(token.getID());
        if (tc != null && tc.getCurrentCode() != null)
            tl.start(token.getType(), tc, false);
    }

    @Override
    protected View createView(ViewGroup parent, int type) {
        return mLayoutInflater.inflate(R.layout.token, parent, false);
    }
}
