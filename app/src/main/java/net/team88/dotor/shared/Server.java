package net.team88.dotor.shared;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.gson.Gson;

import net.team88.dotor.BuildConfig;
import net.team88.dotor.utils.GsonUtils;
import net.team88.dotor.utils.Utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Eun Leem on 3/21/2016.
 */
public class Server {
    static final String TAG = "SERVER";
    private static final String KEY_COOKIE_DOMAIN = "domain";
    private static final String KEY_COOKIE_SESSION = "dotor_session";
    static Server instance;

    Context context;

    String serverUrl = "https://dotor.team88.net"; // Fallback URL. It is replaced by value in AndroidManifest.xml

    static final String USER = "/user";
    static final String PET = "/pet";
    static final String REVIEW = "/review";

    static final String INSERT = "/insert";
    static final String UPDATE = "/update";
    static final String DELETE = "/delete";


    Status status;
    Date lastTimeStatusChecked;

    Retrofit retrofit;

    SharedPreferences storage;

    String domain;
    String sessionValue;

    private final static HashMap<String, Cookie> cookieStore = new HashMap<>();

    public enum Status {
        OK,
        NO_CONNECTION,
        SERVER_DOWN,
        BUSY,
        UNKNOWN_ERROR
    }

    private Server(Context context) {
        this.context = context;
        this.storage = context.getSharedPreferences("Server", Context.MODE_PRIVATE);

        domain = this.storage.getString(KEY_COOKIE_DOMAIN, "");
        sessionValue = this.storage.getString(KEY_COOKIE_SESSION, "");

        Log.i(TAG, "Domain " + domain);
        Log.i(TAG, "Sess " + sessionValue);

        loadServerUrl();

        Gson gson = GsonUtils.getGson();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS) // It affects Image Upload requests.
                .writeTimeout(5, TimeUnit.SECONDS)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        final String domain = url.host() + ":" + String.valueOf(url.port());
                        for (Cookie c : cookies) {
                            if (c.name().equalsIgnoreCase("dotor_session")) {
                                Server.this.domain = domain;
                                sessionValue = c.toString();
                                storage.edit()
                                        .putString("domain", domain)
                                        .putString("dotor_session", c.toString())
                                        .apply();

                                if (BuildConfig.DEBUG) {
                                    Log.i(TAG, "Cookie saved! " + Server.this.domain + ":" + sessionValue);
                                }
                            }
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        final String domain = url.host() + ":" + String.valueOf(url.port());
                        List<Cookie> list = new ArrayList<>();
                        if (Server.this.domain.equals(domain)) {
                            if (sessionValue.isEmpty() == false) {
                                Cookie cookie = Cookie.parse(url, sessionValue);
                                if (BuildConfig.DEBUG) {
                                    Log.i(TAG, "Cookie loaded! " + Server.this.domain + ": " + cookie.toString());
                                }
                                list.add(cookie);
                            }
                        }
                        return list;
                    }
                });

        clientBuilder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                Response response = chain.proceed(request);

                if (response.isSuccessful() == false) {
                    Crashlytics.log(Log.ERROR, "Network",
                            "Failed to get response from Server. ReqUrl: " +
                                    request.url().toString()
                    );
                }

                Log.i(TAG, "response: " + response.toString());
                Log.i(TAG, "response headers: " + response.headers().toString());

                return response;
            }
        });


        retrofit = new Retrofit.Builder()
                .baseUrl(getServerUrl())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(clientBuilder.build())
                .build();
    }

    public static synchronized Server getInstance(Context context) {
        if (instance == null) {
            instance = new Server(context);
        }
        return instance;
    }

    public void reset() {
        storage.edit().clear().apply();
        instance = null;
    }

    private void loadServerUrl() {
        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;

            serverUrl = bundle.getString("net.team88.server.url");

            if (BuildConfig.DEBUG) {
                serverUrl = bundle.getString("net.team88.server.url.debug");

            } else if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("alpha")) {
                serverUrl = bundle.getString("net.team88.server.url.alpha");

            } else if (BuildConfig.BUILD_TYPE.equalsIgnoreCase("beta")) {
                serverUrl = bundle.getString("net.team88.server.url.beta");

            }

        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Failed to load meta-data, NameNotFound: " + e.getMessage());
            //Crashlytics.log(Log.ERROR, TAG, "Failed to load meta-data. NameNotFound: " + e.getMessage());

        } catch (NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data, NullPointer: " + e.getMessage());
            //Crashlytics.log(Log.ERROR, TAG, "Failed to load meta-data. NullPointer: " + e.getMessage());
        }
    }

    public String getServerUrl() {
        return serverUrl;
    }

    // Currently not working well!
    public Status checkServerStatus() {
        boolean isAvailable = Utils.isNetworkAvailable(context);
        if (isAvailable == false) {
            status = Status.NO_CONNECTION;
        } else {
            // #TODO Make Main thread wait for the result (thread join)
            // Be careful! This is Async function.
            status = Status.OK; // For now, set the Status to OK and fetch the result later.
        }

        lastTimeStatusChecked = new Date();
        return status;
    }

    public Retrofit getRetrofit() {
        return retrofit;
    }

    public DotorWebService getService() {
        DotorWebService webServiceApi = retrofit.create(DotorWebService.class);
        return webServiceApi;
    }

    public String getRegisterUrl() {
        return getServerUrl() + USER + "/register";
    }

    public String getLoginUrl() {
        return getServerUrl() + USER + "/login";
    }

    public String getUserUpdateUrl() {
        return getServerUrl() + USER + UPDATE;
    }

    public String getPetInsertUrl() {
        return getServerUrl() + PET + INSERT;
    }

    public String getPetUpdateUrl() {
        return getServerUrl() + PET + UPDATE;
    }
}
