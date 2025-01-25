package game;

import javax.swing.*;
import java.awt.*;

public class DiscButton extends JButton
{
    public DiscButton(Color color) {
        setBackground(color);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        if (getModel().isArmed()) {graphics.setColor(getBackground().darker());}
        else {graphics.setColor(getBackground());}
        graphics.fillOval(0, 0, getWidth(), getHeight());
        super.paintComponent(graphics);
    }

    @Override
    protected void paintBorder(Graphics graphics) {
        graphics.setColor(getBackground().darker());
        graphics.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(80, 80);
    }

}
