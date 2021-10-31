package com.vs.authenticator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;
import com.vs.authenticator.Token.TokenUriInvalidException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

public class TokenPersistence {
    private static final String NAME = "tokens";
    private static final String ORDER = "tokenOrder";
    private final SharedPreferences prefs;
    private final Gson gson;

    public TokenPersistence(Context ctx) {
        prefs = ctx.getApplicationContext().getSharedPreferences(NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    /**
     * Save token async, because Image needs to be downloaded/copied to storage
     *
     * @param context Application Context
     * @param token   Token (with Image, Image will be saved by the async task)
     */
    public static void saveAsync(Context context, final Token token) {
        File outFile = null;
        if (token.getImage() != null)
            outFile = new File(context.getFilesDir(), "img_" + UUID.randomUUID().toString() + ".png");
        new SaveTokenTask().execute(new TaskParams(token, outFile, context));
    }

    private List<String> getTokenOrder() {
        Type type = new TypeToken<List<String>>() {
        }.getType();
        String str = prefs.getString(ORDER, "[]");
        List<String> order = gson.fromJson(str, type);
        return order == null ? new LinkedList<>() : order;
    }

    private SharedPreferences.Editor setTokenOrder(List<String> order) {
        return prefs.edit().putString(ORDER, gson.toJson(order));
    }

    public int length() {
        return getTokenOrder().size();
    }

    public boolean tokenExists(Token token) {
        return prefs.contains(token.getID());
    }

    public Token get(int position) {
        String key = getTokenOrder().get(position);
        String str = prefs.getString(key, null);

        try {
            return gson.fromJson(str, Token.class);
        } catch (JsonSyntaxException jse) {
            // Backwards compatibility for URL-based persistence.
            try {
                return new Token(str, true);
            } catch (TokenUriInvalidException tuie) {
                tuie.printStackTrace();
            }
        }

        return null;
    }

    public void save(Token token) {
        String key = token.getID();

        //if token exists, just update it
        if (prefs.contains(key)) {
            prefs.edit().putString(token.getID(), gson.toJson(token)).apply();
            return;
        }

        List<String> order = getTokenOrder();
        order.add(0, key);
        setTokenOrder(order).putString(key, gson.toJson(token)).apply();
    }

    public void move(int fromPosition, int toPosition) {
        if (fromPosition == toPosition)
            return;

        List<String> order = getTokenOrder();
        if (fromPosition < 0 || fromPosition > order.size())
            return;
        if (toPosition < 0 || toPosition > order.size())
            return;

        order.add(toPosition, order.remove(fromPosition));
        setTokenOrder(order).apply();
    }

    public void delete(int position) {
        List<String> order = getTokenOrder();
        String key = order.remove(position);
        setTokenOrder(order).remove(key).apply();
    }

    /**
     * Data class for SaveTokenTask
     */
    private static class ReturnParams {
        private final Token token;
        private final Context context;

        public ReturnParams(Token token, Context context) {
            this.token = token;
            this.context = context;
        }

        public Token getToken() {
            return token;
        }

        public Context getContext() {
            return context;
        }
    }

    /**
     * Data class for SaveTokenTask
     */
    private static class TaskParams {
        private final File outFile;
        private final Context mContext;
        private final Token token;

        public TaskParams(Token token, File outFile, Context mContext) {
            this.token = token;
            this.outFile = outFile;
            this.mContext = mContext;
        }

        public Context getContext() {
            return mContext;
        }

        public Token getToken() {
            return token;
        }

        public File getOutFile() {
            return outFile;
        }
    }

    /**
     * Downloads/copies images to FreeOTP storage
     * Saves token in PostExecute
     */
    private static class SaveTokenTask extends AsyncTask<TaskParams, Void, ReturnParams> {
        protected ReturnParams doInBackground(TaskParams... params) {
            final TaskParams taskParams = params[0];
            if (taskParams.getToken().getImage() != null) {
                try {
                    Bitmap bitmap = Picasso.with(taskParams.getContext())
                            .load(taskParams.getToken()
                                    .getImage())
                            .resize(200, 200)   // it's just an icon
                            .onlyScaleDown()    //resize image, if bigger than 200x200
                            .get();
                    File outFile = taskParams.getOutFile();
                    //saveAsync image
                    FileOutputStream out = new FileOutputStream(outFile);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, out);
                    out.close();
                    taskParams.getToken().setImage(Uri.fromFile(outFile));
                } catch (IOException e) {
                    e.printStackTrace();
                    //set image to null to prevent internet link in image, in case image
                    //was scanned, when no connection existed
                    taskParams.getToken().setImage(null);
                }
            }
            return new ReturnParams(taskParams.getToken(), taskParams.getContext());
        }

        @Override
        protected void onPostExecute(ReturnParams returnParams) {
            super.onPostExecute(returnParams);
            //we downloaded the image, now save it normally
            new TokenPersistence(returnParams.getContext()).save(returnParams.getToken());
            //refresh TokenAdapter
            returnParams.context.sendBroadcast(new Intent(MainActivity.ACTION_IMAGE_SAVED));
        }
    }
}