package com.maple.audiometry.utils.permission;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import java.util.ArrayList;

/**
 * @author maple
 * @time 2018/2/6
 */
public class PermissionFragment extends Fragment {

    public static final String TAG = "PermissionFragment";

    public static final int PERMISSIONS_REQUEST_CODE = 42;
    public static final int REQUEST_APP_SETTINGS = 43;

    private PermissionListener mPermissionListener;
    public boolean isStatus;

    public static PermissionFragment getPermissionFragment(Activity activity) {
        PermissionFragment permissionFragment = (PermissionFragment)
                activity.getFragmentManager().findFragmentByTag(PermissionFragment.TAG);
        if (permissionFragment == null) {
            permissionFragment = new PermissionFragment();
            FragmentManager fragmentManager = activity.getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(permissionFragment, PermissionFragment.TAG)
                    .commit();
            fragmentManager.executePendingTransactions();
        }
        return permissionFragment;
    }

    public PermissionFragment setPermissionListener(PermissionListener permissionListener) {
        mPermissionListener = permissionListener;
        return this;
    }

    public void checkPermissions(@NonNull String[] permissions, String permissionMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> needPermissions = new ArrayList();

            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                    needPermissions.add(permission);
                }
            }

            if (needPermissions.isEmpty()) {
                granted();
                return;
            }

            String[] string = new String[needPermissions.size()];
            needPermissions.toArray(string);


            if (permissionMessage != null && !TextUtils.isEmpty(permissionMessage)) {
                showRequestPermissionDialog(string, permissionMessage);
            } else {
                requestPermissions(string);
            }
        } else {
            granted();
        }
    }

    protected void showRequestPermissionDialog(@NonNull final String[] permissions, String permissionMessage) {
        AlertDialog builder = new AlertDialog.Builder(getActivity())
                .setMessage(permissionMessage)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        dialog.dismiss();
                        denied(permissions);
                    }
                })
                .setPositiveButton(getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(permissions);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        denied(permissions);
                    }
                }).create();

        builder.setCanceledOnTouchOutside(false);
        builder.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void requestPermissions(@NonNull String[] permissions) {
        isStatus = shouldShowRequestPermissionRationale(permissions[0]);
        requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_CODE:
                ArrayList<String> deniedPermissions = new ArrayList<>();
                if (permissions.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            deniedPermissions.add(permissions[i]);
                        }
                    }
                }
                if (deniedPermissions.size() > 0) {
                    String[] string = new String[deniedPermissions.size()];
                    string = deniedPermissions.toArray(string);
                    denied(string);
                    if (!shouldShowRequestPermissionRationale(permissions[0]) && !isStatus) {
                        if (mPermissionListener != null) {
                            mPermissionListener.onPermissionDeniedDotAgain(string);
                        }
                    }
                } else {
                    granted();
                }
                break;
            case REQUEST_APP_SETTINGS:
                break;
        }
    }

    protected void granted() {
        if (mPermissionListener != null) {
            mPermissionListener.onPermissionGranted();
        }
    }

    protected void denied(@NonNull String[] permissions) {
        if (mPermissionListener != null) {
            mPermissionListener.onPermissionDenied(permissions);
        }
    }

}
