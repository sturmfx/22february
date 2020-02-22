package org.example;

import com.vaadin.flow.component.ClickNotifier;
import org.vaadin.pekkam.Canvas;

public class GameField extends Canvas implements ClickNotifier<Canvas>
{
    public GameField(int width, int height)
    {
        super(width, height);
    }
}
