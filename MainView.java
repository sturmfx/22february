package org.example;

import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import org.vaadin.pekkam.Canvas;
import org.vaadin.pekkam.CanvasRenderingContext2D;

import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

/**
 * The main view contains a button and a click listener.
 */
@Route
@PWA(name = "My Application", shortName = "My Application")
public class MainView extends HorizontalLayout
{
    public static ArrayList<String> results = new ArrayList<>();


    public ArrayList<String> results1 = new ArrayList<>();
    Random random = new Random();

    int x = 20;
    int y = 10;

    int easy_mine_amount = 5;
    int medium_mine_amount = 35;
    int hard_mine_amount = 45;

    int mine_amount = 25;

    int pixel = 50;
    int border = 2;

    int flags_set = 0;
    boolean first_click = false;
    boolean boom = false;
    boolean win = false;

    long start_time = 0;
    long end_time = 0;

    boolean[][] is_visible = new boolean[x][y];
    boolean[][] is_flag = new boolean[x][y];
    boolean[][] is_mine = new boolean[x][y];
    int[][] mines = new int[x][y];

    Button easy = new Button("EASY", event -> easy());
    Button medium = new Button("MEDIUM", event -> medium());
    Button hard = new Button("HARD", event -> hard());
    Button refresh = new Button("REFRESH", event -> update_data());
    TextField username = new TextField();
    Label report = new Label();
    GameField canvas = new GameField(1000, 500);
    CanvasRenderingContext2D gc = canvas.getContext();

    VerticalLayout game = new VerticalLayout();
    VerticalLayout data = new VerticalLayout();

    HorizontalLayout buttons = new HorizontalLayout();

    public MainView()
    {
        this.setMargin(false);
        this.setPadding(false);
        this.setSpacing(false);

        game.setMargin(false);
        game.setPadding(false);
        game.setSpacing(false);

        data.setMargin(false);
        data.setPadding(false);
        data.setSpacing(false);

        canvas.addClickListener(event -> canvas(event));

        username.setPlaceholder("USERNAME");
        buttons.add(easy, medium, hard, username, refresh);

        game.add(canvas);
        game.add(report);
        game.add(buttons);

        add(game);
        add(data);

        report.setMinWidth(String.valueOf(pixel * x) + "px");

        reset();
        draw();
    }

    public void reset()
    {
        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++)
            {
                is_visible[i][j] = false;
                is_flag[i][j] = false;
                is_mine[i][j] = false;
                mines[i][j] = 0;
            }
        }
        flags_set = 0;
        first_click = false;
        boom = false;
        win = false;
        start_time = System.nanoTime();
    }

    public void start(int fx, int fy)
    {
        int temp_mines = 0;

        while(temp_mines < mine_amount)
        {
            int tx = random.nextInt(x);
            int ty = random.nextInt(y);

            if(!is_mine[tx][ty]&&(tx != fx)&&(ty != fy))
            {
                is_mine[tx][ty] = true;
                temp_mines++;
            }
        }

        if(is_flag[fx][fy])
        {
            is_flag[fx][fy] = false;
            flags_set--;
        }

        first_click = true;
        draw();
    }

    public boolean is_win()
    {
        int temp_mines = 0;
        boolean result = true;
        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++)
            {
                if(is_mine[i][j])
                {
                    temp_mines++;
                    result = result && is_flag[i][j];
                }
            }
        }
        if(temp_mines == 0)
        {
            return false;
        }
        else
        {
            return result;
        }

    }

    public void calc_flags()
    {
        int flags = 0;

        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++)
            {
                if(is_visible[i][j] && is_flag[i][j])
                {
                    is_flag[i][j] = false;
                }

                if(is_flag[i][j])
                {
                    flags++;
                }
            }
        }

        flags_set = flags;
    }

    public void flood_fill(int x, int y)
    {
        boolean[][] hits = new boolean[this.x][this.y];
        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(x, y));

        while (!queue.isEmpty())
        {
            Point p = queue.remove();

            if(flood_fill_1(hits,p.x,p.y))
            {
                queue.add(new Point(p.x,p.y - 1));
                queue.add(new Point(p.x,p.y + 1));
                queue.add(new Point(p.x - 1,p.y));
                queue.add(new Point(p.x + 1,p.y));
            }
        }
    }

    public boolean flood_fill_1(boolean[][] hits, int x, int y)
    {
        if (x < 0 || x >= this.x || y < 0 || y >= this.y) return false;
        if(hits[x][y])
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            return false;
        }
        if (mines[x][y] != 0)
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            return false;
        }

        if(is_flag[x][y])
        {
            is_flag[x][y] = false;
            flags_set--;
        }

        is_visible[x][y] = true;

        if((x > 0)&&(y > 0))
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x - 1][y - 1] = true;
        }

        if(y > 0)
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x][y - 1] = true;
        }

        if((x < this.x - 1)&&(y > 0))
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x + 1][y - 1] = true;
        }

        if(x > 0)
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x - 1][y] = true;
        }


        if(x < this.x - 1)
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x + 1][y] = true;
        }

        if((x > 0)&&(y < this.y - 1))
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x - 1][y + 1] = true;
        }

        if(y < this.y - 1)
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x][y + 1] = true;
        }

        if((x < this.x - 1)&&(y < this.y - 1))
        {
            if(is_flag[x][y])
            {
                is_flag[x][y] = false;
                flags_set--;
            }
            is_visible[x + 1][y + 1] = true;
        }

        hits[x][y] = true;
        return true;
    }

    public void calculate()
    {
        for(int i = 0; i < this.x; i++)
        {
            for(int j = 0; j < this.y; j++)
            {
                if(!is_mine[i][j])
                {
                    int mines_around = 0;

                    //1
                    if((i > 0)&&(j > 0))
                    {
                        if(is_mine[i - 1][j - 1])
                        {
                            mines_around++;
                        }
                    }

                    //2
                    if(j > 0)
                    {
                        if(is_mine[i][j - 1])
                        {
                            mines_around++;
                        }
                    }

                    //3
                    if((i < this.x - 1)&&(j > 0))
                    {
                        if(is_mine[i + 1][j - 1])
                        {
                            mines_around++;
                        }
                    }

                    //4
                    if(i > 0)
                    {
                        if(is_mine[i - 1][j])
                        {
                            mines_around++;
                        }
                    }

                    //6
                    if(i < this.x - 1)
                    {
                        if(is_mine[i + 1][j])
                        {
                            mines_around++;
                        }
                    }

                    //7
                    if((i > 0)&&(j < this.y - 1))
                    {
                        if(is_mine[i - 1][j + 1])
                        {
                            mines_around++;
                        }
                    }

                    //8
                    if(j < this.y - 1)
                    {
                        if(is_mine[i][j + 1])
                        {
                            mines_around++;
                        }
                    }

                    //9
                    if((i < this.x - 1)&&(j < this.y - 1))
                    {
                        if(is_mine[i + 1][j + 1])
                        {
                            mines_around++;
                        }
                    }
                    mines[i][j] = mines_around;
                }
            }
        }
    }


    public void canvas(ClickEvent event)
    {
        int tx = (int) (event.getClientX() / pixel);
        int ty = (int) (event.getClientY() / pixel);

        if(!win)
        {
            if (!boom)
            {
                if (!event.isCtrlKey())
                {
                    if (!first_click)
                    {
                        start(tx, ty);
                        calculate();
                        if (mines[tx][ty] == 0)
                        {
                            flood_fill(tx, ty);
                        }
                        else
                        {
                            if(is_flag[tx][ty])
                            {
                                is_flag[tx][ty] = false;
                                flags_set--;
                            }

                            is_visible[tx][ty] = true;
                        }

                    }
                    else
                    {
                        if (is_mine[tx][ty])
                        {
                            boom = true;
                        }
                        else
                        {
                            if (mines[tx][ty] == 0)
                            {
                                flood_fill(tx, ty);
                            }
                            else
                            {
                                if(is_flag[tx][ty])
                                {
                                    is_flag[tx][ty] = false;
                                    flags_set--;
                                }

                                is_visible[tx][ty] = true;
                            }
                        }
                    }

                } else
                {
                    if (!is_visible[tx][ty])
                    {
                        if (!is_flag[tx][ty])
                        {
                            if (flags_set < mine_amount)
                            {
                                is_flag[tx][ty] = true;
                                flags_set++;
                            }
                        }
                        else
                        {
                            is_flag[tx][ty] = false;
                            flags_set--;
                        }
                    }
                }


            }
            if(is_win())
            {
                win = true;
                end_time = System.nanoTime();
                long seconds  = (end_time - start_time)/1000000000;
                data(true, "PLAYER: " + username.getValue() + " has won in " + seconds + " seconds");
                update_data();
            }
            calc_flags();
            draw();
        }
    }

    public void draw()
    {
        gc.setFillStyle("white");
        gc.fillRect(0, 0, pixel * x, pixel * y);
        for(int i = 0; i < x; i++)
        {
            for(int j = 0; j < y; j++)
            {
                gc.setStrokeStyle("black");
                gc.setLineWidth(border);
                gc.strokeRect(pixel * i, pixel * j, pixel, pixel);

                if(is_visible[i][j])
                {
                    if(mines[i][j] == 0)
                    {
                        gc.setFillStyle("white");
                        gc.fillRect(pixel * i + border, pixel * j + border, pixel - 2 * border, pixel - 2 * border);
                    }
                    else
                    {
                        gc.setFillStyle("white");
                        gc.fillRect(pixel * i + border, pixel * j + border, pixel - 2 * border, pixel - 2 * border);

                        gc.setFillStyle("green");
                        gc.setFont("20px serif");
                        gc.fillText(String.valueOf(mines[i][j]), pixel * i + pixel * 0.5 - 3, pixel * j + pixel * 0.5);
                    }

                }
                else
                {
                    if(is_flag[i][j])
                    {
                        gc.setFillStyle("wheat");
                        gc.fillRect(pixel * i + border, pixel * j + border, pixel - 2 * border, pixel - 2 * border);

                        gc.setFillStyle("red");
                        gc.fillRect(pixel * i + 10, pixel * j + 10, pixel - 20, pixel - 20);
                    }
                    else
                    {
                        gc.setFillStyle("wheat");
                        gc.fillRect(pixel * i + border, pixel * j + border, pixel - 2 * border, pixel - 2 * border);
                    }
                }

                if(boom && is_mine[i][j])
                {
                    gc.setFillStyle("wheat");
                    gc.fillRect(pixel * i + border, pixel * j + border, pixel - 2 * border, pixel - 2 * border);
                    gc.setFillStyle("purple");

                    gc.fillRect(pixel * i + 10, pixel * j + 10, pixel - 20, pixel - 20);

                    gc.setFillStyle("blue");
                    gc.setFont("100px serif");
                    gc.fillText("LOST", 0, 100 );
                }
            }
        }
        report.setText("FLAGS SET: " + flags_set);
        if(is_win())
        {
            gc.setFillStyle("blue");
            gc.setFont("100px serif");
            gc.fillText("WIN", 0, 100 );
        }
    }

    public void easy()
    {
        mine_amount = easy_mine_amount;
        reset();
        draw();
    }

    public void medium()
    {
        mine_amount = medium_mine_amount;
        reset();
        draw();
    }

    public void hard()
    {
        mine_amount = hard_mine_amount;
        reset();
        draw();
    }



    public synchronized void data(boolean add, String data)
    {
        if(add)
        {
            results.add(data);
        }
        this.results1 = (ArrayList) results.clone();
    }

    public void update_data()
    {
        data(false, "");
        data.removeAll();
        for(String s: results1)
        {
            data.add(new Label(s));
        }
    }

}
