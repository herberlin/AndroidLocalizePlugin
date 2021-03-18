/*
 * Copyright 2018 Airsaid. https://github.com/airsaid
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import constant.Constants;
import logic.LanguageHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import translate.lang.LANG;
import translate.trans.impl.GoogleTranslator;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Select the language dialog you want to convert.
 *
 * @author airsaid
 */
public class SelectLanguageDialog extends DialogWrapper {
    private JPanel myPanel;
    private JCheckBox overwriteExistingStringCheckBox;
    private JCheckBox selectAllCheckBox;
    private JPanel languagesPanel;
    private JCheckBox certCheckbox;
    private JTextField certInput;
    private JButton certButton;
    private JTextPane getYourCertificateAtTextPane;
    private JScrollPane languageScrollPanel;

    private Project mProject;
    private OnClickListener mOnClickListener;
    private List<LANG> mSelectLanguages = new ArrayList<>();

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }

    public interface OnClickListener {
        void onClickListener(List<LANG> selectedLanguage);
    }

    public SelectLanguageDialog(@Nullable Project project) {
        super(project, false);
        this.mProject = project;
        doCreateCenterPanel();
        setTitle("Select Convert Languages");
        setResizable(true);
        init();
    }

    public void setOnClickListener(OnClickListener listener) {
        mOnClickListener = listener;
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myPanel;
    }

    private void doCreateCenterPanel() {
        // add language
        mSelectLanguages.clear();
        List<LANG> supportLanguages = new GoogleTranslator().getSupportLang();
        List<String> selectedLanguageCodes = LanguageHelper.getSelectedLanguageCodes(mProject);
        // sort by country code, easy to find
        supportLanguages.sort(new CountryCodeComparator());
        languagesPanel.setLayout(new GridLayout(supportLanguages.size() / 4, 4));
        for (LANG language : supportLanguages) {
            String code = language.getCode();
            JBCheckBox checkBoxLanguage = new JBCheckBox();
            checkBoxLanguage.setText(language.getEnglishName()
                    .concat("(").concat(code).concat(")"));
            languagesPanel.add(checkBoxLanguage);
            checkBoxLanguage.addItemListener(e -> {
                int state = e.getStateChange();
                if (state == ItemEvent.SELECTED) {
                    mSelectLanguages.add(language);
                } else {
                    mSelectLanguages.remove(language);
                }
            });
            if (selectedLanguageCodes != null && selectedLanguageCodes.contains(code)) {
                checkBoxLanguage.setSelected(true);
            }
        }

        final PropertiesComponent props = PropertiesComponent.getInstance(mProject);

        boolean isOverwriteExistingString = props.getBoolean(Constants.KEY_IS_OVERWRITE_EXISTING_STRING);
        overwriteExistingStringCheckBox.setSelected(isOverwriteExistingString);
        overwriteExistingStringCheckBox.addItemListener(e -> {
            int state = e.getStateChange();
            props.setValue(Constants.KEY_IS_OVERWRITE_EXISTING_STRING, state == ItemEvent.SELECTED);
        });

        boolean isSelectAll = props.getBoolean(Constants.KEY_IS_SELECT_ALL);
        selectAllCheckBox.setSelected(isSelectAll);
        selectAllCheckBox.addItemListener(e -> {
            int state = e.getStateChange();
            selectAll(state == ItemEvent.SELECTED);
            props.setValue(Constants.KEY_IS_SELECT_ALL, state == ItemEvent.SELECTED);
        });


        // use payed cloud service checkbox
        certCheckbox.setSelected(props.getBoolean(Constants.KEY_USE_GOOGLE_CLOUD_SERVICE));
        certCheckbox.addItemListener(e -> {
            props.setValue(Constants.KEY_USE_GOOGLE_CLOUD_SERVICE, e.getStateChange() == ItemEvent.SELECTED);
        });

        // certificate location
        certInput.setText(props.getValue(Constants.KEY_GOOGLE_CLOUD_SERVICE_CERT_PATH, ""));
        certInput.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent documentEvent) {
                props.setValue(Constants.KEY_GOOGLE_CLOUD_SERVICE_CERT_PATH, certInput.getText());
            }
        });

        // certification choose button
        certButton.addActionListener(a -> {
            JFileChooser fileChooser = new JFileChooser(props.getValue(Constants.KEY_GOOGLE_CLOUD_SERVICE_CERT_PATH));
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            fileChooser.setFileFilter(new FileFilter() {
                @Override
                public boolean accept(File f) {
                    return f!= null && f.getName().endsWith(".json");
                }

                @Override
                public String getDescription() {
                    return ".json";
                }
            });
            if (fileChooser.showOpenDialog(myPanel) == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                if (file != null && file.exists() && file.isFile() && file.canRead()) {
                    String cert = file.toString();
                    certInput.setText(cert);
                    props.setValue(Constants.KEY_GOOGLE_CLOUD_SERVICE_CERT_PATH, cert);
                }
            }
        });
    }

    private void selectAll(boolean selectAll) {
        for (Component component : languagesPanel.getComponents()) {
            if (component instanceof JBCheckBox) {
                JBCheckBox checkBox = (JBCheckBox) component;
                checkBox.setSelected(selectAll);
            }
        }
    }

    @Override
    protected void doOKAction() {
        LanguageHelper.saveSelectedLanguage(mProject, mSelectLanguages);
        if (mSelectLanguages.size() <= 0) {
            Messages.showErrorDialog("Please select the language you need to translate!", "Error");
            return;
        }
        if (mOnClickListener != null) {
            mOnClickListener.onClickListener(mSelectLanguages);
        }
        super.doOKAction();
    }

    class CountryCodeComparator implements Comparator<LANG> {
        @Override
        public int compare(LANG o1, LANG o2) {
            return o1.getCode().compareTo(o2.getCode());
        }
    }
}
