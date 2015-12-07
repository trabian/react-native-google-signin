package co.apptailor.googlesignin;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.util.Log;
import android.support.v4.app.FragmentActivity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.UiThreadUtil;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;

public class RNGoogleSigninModule
    extends ReactContextBaseJavaModule
    implements GoogleApiClient.OnConnectionFailedListener {

    private FragmentActivity mActivity;
    private GoogleApiClient mApiClient;

    private static ReactApplicationContext mContext;
    
    private static final String TAG = "BillHero";

    public static final int RC_GET_AUTH_CODE = 9003;

    public RNGoogleSigninModule(final ReactApplicationContext reactContext, FragmentActivity activity) {
        
        super(reactContext);
        mContext = reactContext;
        mActivity = activity;
        
    }

    @Override
    public String getName() {
        return "GoogleSignin";
    }
    
    @ReactMethod
    public void init(final String clientId) {

       UiThreadUtil.runOnUiThread (
            new Runnable() {
                @Override
                public void run() {
        
                    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(clientId)
                        .requestServerAuthCode(clientId)
                        .requestEmail()
                        .build();
                    
                    mApiClient = new GoogleApiClient.Builder(mActivity)
                        .enableAutoManage(mActivity, RNGoogleSigninModule.this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();
                    
                    start();
                    
                }
            });
        
    }
    
    @ReactMethod
    public void signIn() {
        
        UiThreadUtil.runOnUiThread(new Runnable() {
                
            @Override
            public void run() {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mApiClient);
                mActivity.startActivityForResult(signInIntent, RC_GET_AUTH_CODE);
            }
                
        });
        
    }

    @ReactMethod
    public void signOut() {
        Auth.GoogleSignInApi.signOut(mApiClient).setResultCallback(
                new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {

                    }
                });
    }

    private void start() {
        
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mApiClient);

        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    public static void onActivityResult(Intent data, int resultCode) {
        GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
        handleSignInResult(result);
    }

    private static void handleSignInResult(GoogleSignInResult result) {
        
        WritableMap params = Arguments.createMap();

        if (result.isSuccess()) {
            
            GoogleSignInAccount acct = result.getSignInAccount();
            params.putString("name", acct.getDisplayName());
            params.putString("email", acct.getEmail());
            params.putString("idToken", acct.getIdToken());
            params.putString("accessToken", acct.getServerAuthCode());

            mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("googleSignIn", params);
        } else {
            
            params.putInt("error", result.getStatus().getStatusCode());

            mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                    .emit("googleSignInError", params);
        }
    }
        
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        
        if (!connectionResult.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), mActivity, 0).show();
            return;
        }

        try {
            connectionResult.startResolutionForResult(mActivity, RC_GET_AUTH_CODE);
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
        }
        
    }
    
    // private Context _activityContext;
    // private GoogleApiClient _apiClient;    

    // private Activity _activity;
    // private static ReactApplicationContext _context;

    // public static final int RC_SIGN_IN = 9001;



    // @ReactMethod
    // public void signOut() {
    //     Auth.GoogleSignInApi.signOut(_apiClient).setResultCallback(
    //             new ResultCallback<Status>() {
    //                 @Override
    //                 public void onResult(Status status) {

    //                 }
    //             });
    // }


    // @Override
    // public void onConnectionFailed(ConnectionResult connectionResult) {
    //     if (!connectionResult.hasResolution()) {
    //         GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), _activity, 0).show();
    //         return;
    //     }

    //     try {
    //         connectionResult.startResolutionForResult(_activity, RC_SIGN_IN);
    //     } catch (IntentSender.SendIntentException e) {
    //         e.printStackTrace();
    //     }
    // }


}

// public class RNGoogleSigninModule
//         extends ReactContextBaseJavaModule
//         implements GoogleApiClient.OnConnectionFailedListener {

//     private Context _activityContext;
//     private GoogleApiClient _apiClient;    

//     private Activity _activity;
//     private static ReactApplicationContext _context;

//     public static final int RC_SIGN_IN = 9001;

//     public RNGoogleSigninModule(final ReactApplicationContext reactContext, Context activityContext) {
//         super(reactContext);
//         // _activity = activity;
//         _activityContext = activityContext;        
//         _context = reactContext;
//     }

//     @Override
//     public String getName() {
//         return "GoogleSignin";
//     }

//     @ReactMethod
//     public void init(final String clientId) {

//        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
//            .requestServerAuthCode(clientId)
//            .requestEmail()
//            .build();

//        _apiClient = new GoogleApiClient.Builder(_activityContext)
//            .enableAutoManage(_activityContext, this)
//            .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
//            .build();

//        // _apiClient.connect();

//        // start();
        
//     }

//     @ReactMethod
//     public void signIn() {
//         _activity.runOnUiThread(new Runnable() {
                
//             @Override
//             public void run() {
//                 Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(_apiClient);
//                 _activity.startActivityForResult(signInIntent, RC_SIGN_IN);
//             }
                
//         });
//     }


//     public static void onActivityResult(Intent data) {
//         GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
//         handleSignInResult(result);
//     }

//     @Override
//     public void onConnectionFailed(ConnectionResult connectionResult) {
//         if (!connectionResult.hasResolution()) {
//             GooglePlayServicesUtil.getErrorDialog(connectionResult.getErrorCode(), _activity, 0).show();
//             return;
//         }

//         try {
//             connectionResult.startResolutionForResult(_activity, RC_SIGN_IN);
//         } catch (IntentSender.SendIntentException e) {
//             e.printStackTrace();
//         }
//     }

//     private void start() {
//         OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(_apiClient);

//         if (opr.isDone()) {
//             GoogleSignInResult result = opr.get();
//             handleSignInResult(result);
//         } else {
//             opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
//                 @Override
//                 public void onResult(GoogleSignInResult googleSignInResult) {
//                     handleSignInResult(googleSignInResult);
//                 }
//             });
//         }
//     }

//     private static void handleSignInResult(GoogleSignInResult result) {
//         WritableMap params = Arguments.createMap();

//         if (result.isSuccess()) {
            
//             GoogleSignInAccount acct = result.getSignInAccount();
//             params.putString("name", acct.getDisplayName());
//             params.putString("email", acct.getEmail());
//             params.putString("idToken", acct.getIdToken());
//             params.putString("accessToken", acct.getServerAuthCode());

//             _context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                     .emit("googleSignIn", params);
//         } else {
            
//             params.putInt("error", result.getStatus().getStatusCode());

//             _context.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
//                     .emit("googleSignInError", params);
//         }
//     }
// }
