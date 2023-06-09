package com.raven.swing;

import com.raven.event.EventTimeChange;
import com.raven.event.EventTimeSelected;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Arc2D;
import java.awt.geom.Rectangle2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.animation.timing.TimingTargetAdapter;

public class TimeComponent extends JComponent {

    public int getTime_minute() {
        return time_minute;
    }

    public void setTime_minute(int time_minute) {
        this.time_minute = time_minute;
    }

    public int getTime_hour() {
        return time_hour;
    }

    public void setTime_hour(int time_hour) {
        this.time_hour = time_hour;
    }

    public void setEventTimeChange(EventTimeChange eventTimeChange) {
        this.eventTimeChange = eventTimeChange;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    private static final float RAD_PER_NUM = (float) (Math.PI / -6);
    private static final int OFFSET_24HOUR = 27;
    private Color color = new Color(237, 88, 207);
    private int time_minute = 0;
    private int time_hour = 0;
    private int currentSelected = 12;
    private int selectedHour = 12;
    private final Animator animator;
    private float hourAnimat = 12;
    private float betweenHour = 0;
    private int targetHour = 12;
    private float lastTarget;
    private boolean isHour = true;
    private List<EventTimeSelected> events;
    private EventTimeChange eventTimeChange;
    private boolean m_24hourclock = false;
    
    public TimeComponent() {
        events = new ArrayList<>();
        setBackground(Color.WHITE);
        setForeground(new Color(50, 50, 50));
        setPreferredSize(new Dimension(201, 201));
        setFont(new java.awt.Font("sansserif", 0, 14));
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    if (isHour) {
                        setTime_hour(getSelectedHour(me));
                        if (getTime_hour() == -1) {
                            setTime_hour(currentSelected);
                        }
                        runEvent(getTime_hour());
                        changeToMinute();
                    } else {
                        checkMouseSelect(me);
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent me) {
                if (SwingUtilities.isLeftMouseButton(me)) {
                    checkMouseSelectDragged(me);
                }
            }
        });
        TimingTarget target = new TimingTargetAdapter() {
            @Override
            public void timingEvent(float fraction) {
                hourAnimat = lastTarget + betweenHour * fraction;
                repaint();
            }

            @Override
            public void end() {
                selectedHour = targetHour;
                lastTarget = hourAnimat;
                if (isHour) {
                    setTime_hour(selectedHour);
                } else {
                    setTime_minute(selectedHour);
                }
                repaint();
                runEvent(selectedHour);
            }
        };
        animator = new Animator(200, target);
        animator.setResolution(0);
    }

    private void checkMouseSelect(MouseEvent me) {
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        int space = isHour ? 30 : 6;
        int start = 90;
        int max = isHour ?  12 : 59;
        for (int i = isHour ? 1 : 0; i <= max; i++) {
            int add12hour = 0;
            if (isHour) {
                Shape s24 = new Arc2D.Double(x+OFFSET_24HOUR, y+OFFSET_24HOUR, size - 2*OFFSET_24HOUR, size-2*OFFSET_24HOUR, start - (space * i) - (space / 2), space, Arc2D.PIE);
                if (s24.contains(me.getPoint())) {
                    add12hour=12;
                }
            }
                    
            Shape s = new Arc2D.Double(x, y, size, size, start - (space * i) - (space / 2), space, Arc2D.PIE);
            if (s.contains(me.getPoint())) {
                i += add12hour;
                currentSelected = i;
                if (i != selectedHour) {
                    targetHour = i;
                    if (animator.isRunning()) {
                        animator.stop();
                        lastTarget = convertLastTarget(lastTarget);
                    } else {
                        lastTarget = selectedHour;
                    }
                    betweenHour = calulateHour(lastTarget, targetHour);
                    animator.start();
                }
                break;
            }
        }
    }

    private void checkMouseSelectDragged(MouseEvent me) {
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        int space = isHour ? 30 : 6;
        int start = 90;
        int max = isHour ? 12 : 59;
        for (int i = isHour ? 1 : 0; i <= max; i++) {
            int add12hour = 0;
            if (isHour) {
                Shape s24 = new Arc2D.Double(x+OFFSET_24HOUR, y+OFFSET_24HOUR, size - 2*OFFSET_24HOUR, size-2*OFFSET_24HOUR, start - (space * i) - (space / 2), space, Arc2D.PIE);
                if (s24.contains(me.getPoint())) {
                    add12hour=12;
                }
            }
            Shape s = new Arc2D.Double(x, y, size, size, start - (space * i) - (space / 2), space, Arc2D.PIE);
            if (s.contains(me.getPoint())) {
                if (animator.isRunning()) {
                    animator.stop();
                }
                i+= add12hour;
                currentSelected = i;
                if (i != selectedHour) {
                    hourAnimat = i;
                    selectedHour = i;
                    if (isHour) {
                        time_hour = i;
                    } else {
                        time_minute = i;
                    }
                    runEvent(selectedHour);
                    repaint();
                }
                break;
            }
        }
    }

    private int getSelectedHour(MouseEvent me) {
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int x = (width - size)/ 2;
        int y = (height - size) / 2;
        int space = isHour ? 30 : 6;
        int start = 90;
        int max = isHour ? 12 : 59;

        for (int i = isHour ? 1 : 0; i <= max; i++) {
            int add12hour = 0;
            if (isHour) {
                Shape s24 = new Arc2D.Double(x+OFFSET_24HOUR, y+OFFSET_24HOUR, size - 2*OFFSET_24HOUR, size-2*OFFSET_24HOUR, start - (space * i) - (space / 2), space, Arc2D.PIE);

                if (s24.contains(me.getPoint())) {
                    add12hour=12;
                }
            }

            Shape s = new Arc2D.Double(x, y, size, size, start - (space * i) - (space / 2), space, Arc2D.PIE);

            if (s.contains(me.getPoint())) {
                 return i + add12hour == 24 ? 0 : i+add12hour;
            }
        }
        return -1;
    }

    private float calulateHour(float currentHour, int targetHour) {
        float t = targetHour - currentHour;
        if (t > (isHour ? 6 : 30)) {
            t = -((isHour ? 12 : 60) - t);
        } else if (t <= -(isHour ? 6 : 30)) {
            t += (isHour ? 12 : 60);
        }
        return t;
    }

    
    @Override
    protected void paintComponent(Graphics grphcs) {
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int x = (width - size) / 2;
        int y = (height - size) / 2;
        Graphics2D g2 = (Graphics2D) grphcs;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(getBackground());
        g2.fillOval(x, y, size, size);
        int centerX = width / 2;
        int centerY = height / 2;
        int centerSize = 7;
        g2.setColor(color);
        g2.fillOval(centerX - centerSize / 2, centerY - centerSize / 2, centerSize, centerSize);
        drawLineHour(g2, hourAnimat);
        createNumber(g2);

        g2.dispose();
        super.paintComponent(grphcs);
    }

    private void createNumber(Graphics2D g2) {
        DecimalFormat df = new DecimalFormat("00");
        int width = getWidth();
        int height = getHeight();
        int size = Math.min(width, height);
        int centerX = width / 2;
        int centerY = height / 2;
        for (int i = 12; i >= 1; i--) {
            String number;
            if (isHour) {
                number = i + "";
            } else {
                number = df.format(i == 12 ? 0 : (i * 5));
            }
            Dimension stringSize = getStringSize(g2, number);
            float angle = RAD_PER_NUM * i;
            float sine = (float) Math.sin(angle);
            float cosine = (float) Math.cos(angle);
            int dx = (int) ((size / 2 - 13) * -sine);
            int dy = (int) ((size / 2 - 13) * -cosine);
            if (convertLastTargetToHour(hourAnimat) == i * (isHour ? 1 : 5)) {
                g2.setColor(Color.WHITE);
            } else {
                g2.setColor(getForeground());
            }
            g2.drawString(number, dx + centerX - (stringSize.width / 2), dy + centerY + 5);
            if (isHour && m_24hourclock) {
                number = df.format(i == 12 ? 0 : i+12);
                stringSize = getStringSize(g2, number);
                if (convertLastTargetToHour(hourAnimat) == (i == 12 ? 0 : i+12) ) {
                    g2.setColor(Color.WHITE);
                } else {
                    g2.setColor(getForeground());
                }
                int dx2 = (int) ((size / 2 - 13 - OFFSET_24HOUR) * -sine);
                int dy2 = (int) ((size / 2 - 13 - OFFSET_24HOUR) * -cosine);
                
                g2.drawString(number, dx2  + centerX - (stringSize.width / 2), dy2 + centerY + 5);
            }
        }
    }

    private void drawLineHour(Graphics2D g2, float hour) {
        int width = getWidth();
        int height = getHeight();
        int centerX = width / 2;
        int centerY = height / 2;
        int size = Math.min(width, height) / 2 - 13;
        double rhours = (hour * (isHour ? 30 : 6)) * (Math.PI) / 180;
        // we have a 24 hour clock we need to modify some values
        if (isHour && m_24hourclock) {
            hour = getSelectedHour();
            if (hour > 12 || hour == 0) {
                size -= OFFSET_24HOUR;
                if (hour > 12)
                    rhours = ((hour-12) *  30) * (Math.PI) / 180;
                else // hour == 0;
                    rhours = 0;
            }
        } 
        int toX = centerX + (int) (size * Math.cos(rhours - (Math.PI / 2)));
        int toY = centerY + (int) (size * Math.sin(rhours - (Math.PI / 2)));
        g2.setStroke(new BasicStroke(2f));
        g2.drawLine(centerX, centerY, toX, toY);
        int ovalSize = 23;
        g2.fillOval(toX - (ovalSize / 2), toY - (ovalSize / 2), ovalSize, ovalSize);
    }

    private Dimension getStringSize(Graphics2D g2, String text) {
        FontMetrics ft = g2.getFontMetrics();
        Rectangle2D r2 = ft.getStringBounds(text, g2);
        return new Dimension((int) r2.getWidth(), (int) r2.getHeight());
    }

    private int convertLastTargetToHour(float lastTarget) {
        int value = isHour ? (m_24hourclock ? 24 : 12) : 60;
        if (lastTarget <= 0) {
            lastTarget += value;
        } else if (lastTarget > value) {
            lastTarget -= value;
        }
        if (isHour && m_24hourclock) 
            return getSelectedHour();
        else
            return Math.round(lastTarget);
    }

    private float convertLastTarget(float lastTarget) {
        int value = isHour ? (m_24hourclock ? 24 : 12) : 60;
        if (lastTarget <= 0) {
            lastTarget += value;
        } else if (lastTarget > value) {
            lastTarget -= value;
        }
        return lastTarget;
    }

    private int convertHourToTargetMinute(int hour) {
        
        if ((!m_24hourclock && hour == 12) || (m_24hourclock && hour == 24)) {
            return 0;
        } else {
            return (hour % 12) * 5;
        }
    }

    private int convertMinuteToTargetHour(int minute) {
        if (minute == 0) {
            return 12;
        } else {
            return minute / 5;
        }
    }

    public void set24hour(boolean clock24hour) {
        m_24hourclock = clock24hour;
    }
    
    public int getSelectedHour() {
        return selectedHour;
    }

    public void setSelectedHour(int time_hour, int time_minute) {
        this.time_hour = time_hour;
        this.time_minute = time_minute;
        currentSelected = isHour ? time_hour : time_minute;
        if (currentSelected != selectedHour) {
            targetHour = currentSelected;
            if (animator.isRunning()) {
                animator.stop();
                lastTarget = convertLastTarget(lastTarget);
            } else {
                lastTarget = selectedHour;
            }
            betweenHour = calulateHour(lastTarget, targetHour);
            animator.start();
        }
    }

    public void addEventTimeSelected(EventTimeSelected evnet) {
        this.events.add(evnet);
    }

    public void runEvent(int hour) {
        for (EventTimeSelected event : events) {
            if (isHour) {
                event.hourSelected(hour);
            } else {
                event.minuteSelected(hour);
            }
        }
    }

    public void changeToMinute() {
        if (isHour) {
            eventTimeChange.timeChange(false);
            isHour = false;
            targetHour = time_minute;
            selectedHour = convertHourToTargetMinute(selectedHour);
            if (animator.isRunning()) {
                animator.stop();
                lastTarget = convertLastTarget(lastTarget);
            } else {
                lastTarget = selectedHour;
            }
            betweenHour = calulateHour(lastTarget, targetHour);
            animator.start();
        }
    }

    public void changeToHour() {
        if (!isHour) {
            eventTimeChange.timeChange(true);
            isHour = true;
            targetHour = time_hour;
            selectedHour = convertMinuteToTargetHour(selectedHour);
            if (animator.isRunning()) {
                animator.stop();
                lastTarget = convertLastTarget(lastTarget);
            } else {
                lastTarget = selectedHour;
            }
            betweenHour = calulateHour(lastTarget, targetHour);
            animator.start();
        }
    }
}
