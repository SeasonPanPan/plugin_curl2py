package cn.season.plugin.curl2py.actions;

import cn.season.plugin.curl2py.Curl2pyBundle;
import cn.season.plugin.curl2py.convert.ICode;
import cn.season.plugin.curl2py.convert.PythonConvertor;
import cn.season.plugin.curl2py.utils.NotificationUtil;
import cn.season.plugin.curl2py.utils.Strings;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.Optional;

/**
 * the Curl2py
 *
 * @author PanLongfei
 * @date 2024-11-24
 */
public class Curl2pyAction extends AnAction {

    private static final Logger LOGGER = Logger.getInstance(Curl2pyAction.class);

    private Project project;
    private Editor editor;
    private Document document;
    private final ICode icode = new PythonConvertor();

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        project = e.getProject();
        editor = e.getData(CommonDataKeys.EDITOR);
        if (null == project || null == editor) {
            LOGGER.error("project or editor is null, try again.");
            return;
        }
        document = editor.getDocument();
        LOGGER.info("**** update,editor=" + editor);
    }

    @Override
    public void actionPerformed(AnActionEvent e) {
        if (null == project || null == editor) {
            LOGGER.error("project or editor is null, try again.");
            return;
        }
        final String text = readSelectedOrClipboardCode();
        if (Strings.isEmpty(text)) {
            NotificationUtil.warning(Curl2pyBundle.message("warn.invalid.curl"), project);
            return;
        }

        WriteCommandAction.runWriteCommandAction(project,
                () -> CommandProcessor.getInstance().runUndoTransparentAction(() -> t2c(text))
        );
    }

    public String readSelectedOrClipboardCode() {
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        String text = primaryCaret.getSelectedText();
        if (Strings.isNotBlank(text)) {
            return text;
        }
        try {
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable contents = clipboard.getContents(null);
            if (contents != null && contents.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                text = (String) contents.getTransferData(DataFlavor.stringFlavor);
            }
        } catch (Exception ex) {
            LOGGER.error("There was a error on reading clipboard.", ex);
            NotificationUtil.error("read clipboard error", project);
        }
        return Optional.ofNullable(text).orElse("");
    }


    public void t2c(String text) {
        String code = this.icode.to(text);
        if (Strings.isEmpty(code)) {
            NotificationUtil.warning(Curl2pyBundle.message("warn.convert.result.empty"), project);
            return;
        }

        code = getReplaceKeepIndentCode(code);
        replaceSelectedCode(code);
        reformatSelectedCode(code);
    }

    private String getReplaceKeepIndentCode(String code) {
        code = Strings.convertToLF(code);
        int startColumn = editor.getCaretModel().getPrimaryCaret().getSelectionStartPosition().column;
        String padding = startColumn > 0 ? String.format("%" + startColumn + "s", " ") : "";
        return code.replace("\n", "\n" + padding);
    }

    private void replaceSelectedCode(String code) {
        Caret primaryCaret = editor.getCaretModel().getPrimaryCaret();
        int start = primaryCaret.getSelectionStart();
        int end = primaryCaret.getSelectionEnd();
        document.replaceString(start, end, code);
    }

    private void reformatSelectedCode(String code) {
        int start = editor.getCaretModel().getPrimaryCaret().getSelectionStart();
        int end = start + code.length();
        PsiDocumentManager manager = PsiDocumentManager.getInstance(project);
        PsiFile psiFile = manager.getPsiFile(document);
        if (psiFile != null) {
            CodeStyleManager.getInstance(project).reformatText(psiFile, start, end);
        }
    }


}
