package com.raven.swing;

import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPopupMenu;

public class TimePickerMenu extends JPopupMenu {

    @Override
    protected void paintComponent(Graphics grphcs) {
        grphcs.setColor(getBackground());
        grphcs.fillRect(0, 0, getWidth(), 81);
        grphcs.setColor(Color.WHITE);
        grphcs.fillRect(0, 81, getWidth(), getHeight());
    }
}
