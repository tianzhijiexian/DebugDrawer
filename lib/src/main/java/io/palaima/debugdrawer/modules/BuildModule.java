/*
 * Copyright (C) 2015 Mantas Palaima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.palaima.debugdrawer.modules;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import io.palaima.debugdrawer.BaseDebugModule;
import io.palaima.debugdrawer.DebugWidgetStore;
import io.palaima.debugdrawer.R;
import io.palaima.debugdrawer.util.DebugDrawerUtil;

import static io.palaima.debugdrawer.util.PackageManagerHook.KEY_CURRENT_VERSION_CODE;
import static io.palaima.debugdrawer.util.PackageManagerHook.KEY_CURRENT_VERSION_NAME;

public class BuildModule extends BaseDebugModule {

    private DebugWidgetStore store;

    private PackageInfo info;

    private static final int ID_VERSION_CODE = R.id.version_code_view_id, ID_VERSION_NAME = R.id.version_name_view_id;

    @NonNull
    @Override
    public String getName() {
        return "Build Information";
    }

    @Override
    public void onAttachActivity(Activity activity) {
        super.onAttachActivity(activity);
        try {
            info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public DebugWidgetStore createWidgetStore(DebugWidgetStore.Builder builder) {
        store = builder
                .addText("Version", String.valueOf(info.versionCode), ID_VERSION_CODE)
                .addText("Name", info.versionName, ID_VERSION_NAME)
                .addText("Package", info.packageName)
                .addButton("modify version code/name", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PkgDialog dialog = new PkgDialog();
                        dialog.show(((FragmentActivity) getActivity()).getSupportFragmentManager(), "pkgDialog");
                        dialog.setWidgetStore(store);
                    }
                })
                .build();
        return store;
    }

    public static class PkgDialog extends DialogFragment {

        private DebugWidgetStore widgetStore;

        private EditText codeEt;

        private EditText nameEt;

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new AlertDialog.Builder(getActivity())
                    .setTitle("Input new info")
                    .setView(R.layout.dd_pkg_info_dialog)
                    .setPositiveButton("submit", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor spEditor = DebugDrawerUtil.getSpEditor(getContext());
                            spEditor.putInt(KEY_CURRENT_VERSION_CODE,
                                    Integer.valueOf(codeEt.getText().toString().trim())).apply();
                            spEditor.putString(KEY_CURRENT_VERSION_NAME,
                                    nameEt.getText().toString().trim()).apply();
                            Toast.makeText(getActivity(), "success~", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .create();
        }

        @Override
        public void onStart() {
            super.onStart();
            codeEt = (EditText) getDialog().findViewById(R.id.code_et);
            nameEt = (EditText) getDialog().findViewById(R.id.name_et);

            try {
                PackageInfo info = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                codeEt.setText(String.valueOf(info.versionCode));
                nameEt.setText(info.versionName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                codeEt.setText("N/A");
                nameEt.setText("N/A");
            }
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            super.onDismiss(dialog);
            ((TextView) widgetStore.getDebugWidget(ID_VERSION_CODE).getView()).setText(codeEt.getText());
            ((TextView) widgetStore.getDebugWidget(ID_VERSION_NAME).getView()).setText(nameEt.getText());
        }

        public void setWidgetStore(DebugWidgetStore widgetStore) {
            this.widgetStore = widgetStore;
        }
    }
}
