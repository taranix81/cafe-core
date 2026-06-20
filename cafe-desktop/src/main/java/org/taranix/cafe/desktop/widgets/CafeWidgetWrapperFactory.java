package org.taranix.cafe.desktop.widgets;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.*;
import org.taranix.cafe.beans.annotations.classes.CafeSingleton;

@CafeSingleton
public class CafeWidgetWrapperFactory {

    public TextWidgetWrapper text(Composite parent) {
        return new TextWidgetWrapper(parent);
    }

    public TextWidgetWrapper text(Composite parent, int style) {
        return new TextWidgetWrapper(parent, style);
    }

    public TextWidgetWrapper text(Text existing) {
        return new TextWidgetWrapper(existing);
    }

    public StyledTextWidgetWrapper styledText(Composite parent) {
        return new StyledTextWidgetWrapper(parent);
    }

    public StyledTextWidgetWrapper styledText(StyledText existing) {
        return new StyledTextWidgetWrapper(existing);
    }

    public LabelWidgetWrapper label(Composite parent) {
        return new LabelWidgetWrapper(parent);
    }

    public LabelWidgetWrapper label(Label existing) {
        return new LabelWidgetWrapper(existing);
    }

    public CLabelWidgetWrapper clabel(Composite parent) {
        return new CLabelWidgetWrapper(parent);
    }

    public CLabelWidgetWrapper clabel(CLabel existing) {
        return new CLabelWidgetWrapper(existing);
    }

    public LinkWidgetWrapper link(Composite parent) {
        return new LinkWidgetWrapper(parent);
    }

    public LinkWidgetWrapper link(Link existing) {
        return new LinkWidgetWrapper(existing);
    }

    public CheckButtonWidgetWrapper checkButton(Composite parent) {
        return new CheckButtonWidgetWrapper(parent);
    }

    public RadioButtonWidgetWrapper radioButton(Composite parent) {
        return new RadioButtonWidgetWrapper(parent);
    }

    public ToggleButtonWidgetWrapper toggleButton(Composite parent) {
        return new ToggleButtonWidgetWrapper(parent);
    }

    public CheckButtonWidgetWrapper checkButton(Button existing) {
        return new CheckButtonWidgetWrapper(existing);
    }

    public RadioButtonWidgetWrapper radioButton(Button existing) {
        return new RadioButtonWidgetWrapper(existing);
    }

    public ToggleButtonWidgetWrapper toggleButton(Button existing) {
        return new ToggleButtonWidgetWrapper(existing);
    }

    public SpinnerWidgetWrapper spinner(Composite parent) {
        return new SpinnerWidgetWrapper(parent);
    }

    public SpinnerWidgetWrapper spinner(Spinner existing) {
        return new SpinnerWidgetWrapper(existing);
    }

    public ScaleWidgetWrapper scale(Composite parent) {
        return new ScaleWidgetWrapper(parent);
    }

    public ScaleWidgetWrapper scale(Scale existing) {
        return new ScaleWidgetWrapper(existing);
    }

    public SliderWidgetWrapper slider(Composite parent) {
        return new SliderWidgetWrapper(parent);
    }

    public SliderWidgetWrapper slider(Slider existing) {
        return new SliderWidgetWrapper(existing);
    }

    public ProgressBarWidgetWrapper progressBar(Composite parent) {
        return new ProgressBarWidgetWrapper(parent);
    }

    public ProgressBarWidgetWrapper progressBar(ProgressBar existing) {
        return new ProgressBarWidgetWrapper(existing);
    }

    public ComboWidgetWrapper combo(Composite parent) {
        return new ComboWidgetWrapper(parent);
    }

    public ComboWidgetWrapper combo(Combo existing) {
        return new ComboWidgetWrapper(existing);
    }

    public CComboWidgetWrapper ccombo(Composite parent) {
        return new CComboWidgetWrapper(parent);
    }

    public CComboWidgetWrapper ccombo(CCombo existing) {
        return new CComboWidgetWrapper(existing);
    }

    public ListWidgetWrapper list(Composite parent) {
        return new ListWidgetWrapper(parent);
    }

    public ListWidgetWrapper list(List existing) {
        return new ListWidgetWrapper(existing);
    }

    public DateWidgetWrapper date(Composite parent) {
        return new DateWidgetWrapper(parent);
    }

    public DateWidgetWrapper date(DateTime existing) {
        return new DateWidgetWrapper(existing);
    }

    public TimeWidgetWrapper time(Composite parent) {
        return new TimeWidgetWrapper(parent);
    }

    public TimeWidgetWrapper time(DateTime existing) {
        return new TimeWidgetWrapper(existing);
    }

    public <T> TableWidgetWrapper<T> table(Composite parent, CafeTableRenderer<T> renderer) {
        return new TableWidgetWrapper<>(parent, renderer);
    }

    public <T> TableWidgetWrapper<T> table(Composite parent, int style, CafeTableRenderer<T> renderer) {
        return new TableWidgetWrapper<>(parent, style, renderer);
    }

    public <T> TreeWidgetWrapper<T> tree(Composite parent, CafeTreeRenderer<T> renderer) {
        return new TreeWidgetWrapper<>(parent, renderer);
    }

    public <T> TreeWidgetWrapper<T> tree(Composite parent, int style, CafeTreeRenderer<T> renderer) {
        return new TreeWidgetWrapper<>(parent, style, renderer);
    }

    public TabFolderWidgetWrapper tabFolder(Composite parent) {
        return new TabFolderWidgetWrapper(parent);
    }

    public TabFolderWidgetWrapper tabFolder(TabFolder existing) {
        return new TabFolderWidgetWrapper(existing);
    }

    public CTabFolderWidgetWrapper ctabFolder(Composite parent) {
        return new CTabFolderWidgetWrapper(parent);
    }

    public CTabFolderWidgetWrapper ctabFolder(CTabFolder existing) {
        return new CTabFolderWidgetWrapper(existing);
    }

    public BrowserWidgetWrapper browser(Composite parent) {
        return new BrowserWidgetWrapper(parent);
    }

    public BrowserWidgetWrapper browser(Browser existing) {
        return new BrowserWidgetWrapper(existing);
    }
}
