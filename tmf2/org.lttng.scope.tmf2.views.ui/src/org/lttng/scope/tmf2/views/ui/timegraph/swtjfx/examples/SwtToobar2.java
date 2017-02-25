package org.lttng.scope.tmf2.views.ui.timegraph.swtjfx.examples;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

@NonNullByDefault({})
public class SwtToobar2 {

    private Shell shell;

    public SwtToobar2() {
        Display display = new Display();
        shell = new Shell(display, SWT.SHELL_TRIM);
        shell.setLayout(new FillLayout(SWT.VERTICAL));
        shell.setSize(50, 100);

        ToolBar toolbar = new ToolBar(shell, SWT.FLAT);
        ToolItem itemDrop = new ToolItem(toolbar, SWT.DROP_DOWN);
        itemDrop.setText("drop menu");

        itemDrop.addSelectionListener(new SelectionAdapter() {

            Menu dropMenu = null;

            @Override
            public void widgetSelected(SelectionEvent e) {
                if (dropMenu == null) {
                    dropMenu = new Menu(shell, SWT.POP_UP);
                    shell.setMenu(dropMenu);
                    MenuItem itemCheck = new MenuItem(dropMenu, SWT.CHECK);
                    itemCheck.setText("checkbox");
                    MenuItem itemRadio = new MenuItem(dropMenu, SWT.RADIO);
                    itemRadio.setText("radio1");
                    MenuItem itemRadio2 = new MenuItem(dropMenu, SWT.RADIO);
                    itemRadio2.setText("radio2");
                }

                if (e.detail == SWT.ARROW) {
                    // Position the menu below and vertically aligned with the
                    // the drop down tool button.
                    final ToolItem toolItem = (ToolItem) e.widget;
                    final ToolBar toolBar = toolItem.getParent();

                    Point point = toolBar.toDisplay(new Point(e.x, e.y));
                    dropMenu.setLocation(point.x, point.y);
                    dropMenu.setVisible(true);
                }

            }

        });

        shell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }

        display.dispose();
    }

    public static void main(String[] args) {
        new SwtToobar2();
    }

}