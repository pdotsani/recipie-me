package org.vaadin.example;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import ai.peoplecode.OpenAIConversation;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and use @Route
 * annotation to announce it in a URL as a Spring managed bean.
 * <p>
 * A new instance of this class is created for every new user and every browser
 * tab/window.
 * <p>
 * The main view contains a text field for getting the user name and a button
 * that shows a greeting message in a notification.
 */

@Route("")
@PageTitle("Main")
public class MainView extends AppLayout {
    public MainView() {
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("My Recipe App");
        logo.addClassNames("text-l", "m-m");

        DrawerToggle toggle = new DrawerToggle();

        addToNavbar(true, toggle, logo);
    }

    private void createDrawer() {
        Tabs tabs = new Tabs();
        tabs.add(
                createTab("Recipe", RecipeView.class),
                createTab("Ingredients", IngredientsView.class),
                createTab("About", AboutView.class)
        );
        tabs.setOrientation(Tabs.Orientation.VERTICAL);

        addToDrawer(new VerticalLayout(tabs));
    }

    private Tab createTab(String text, Class<? extends com.vaadin.flow.component.Component> navigationTarget) {
        Tab tab = new Tab();
        tab.add(new RouterLink(text, navigationTarget));
        return tab;
    }
}

@Route(value = "recipe", layout = MainView.class)
@PageTitle("Recipe")
class RecipeView extends VerticalLayout {
    private OpenAIConversation conversation;
    private String recipe = "";
    private String data = "";

    public RecipeView() {
        conversation = new OpenAIConversation("demo", "gpt-4o-mini");
        add(new H1("Search by Recipe"));
        TextField askRecipe = new TextField();
        Paragraph paragraph = new Paragraph();

        askRecipe.setPlaceholder("enter a recipe here");
        askRecipe.setWidth("75%");
        paragraph.setWidth("75%");
        paragraph.setHeight("auto");


        askRecipe.addKeyPressListener(Key.ENTER, event -> {
            recipe = askRecipe.getValue();
            data = conversation.askQuestion("Can you show me a recipe for ", recipe);
            paragraph.setText(data);
        });

        add(askRecipe);
        add(paragraph);
    }
}

@Route(value = "ingredients", layout = MainView.class)
@PageTitle("Ingredients")
class IngredientsView extends VerticalLayout {
    private OpenAIConversation conversation;
    private String ingridients = "";
    private String data = "";

    public IngredientsView() {
        add(new H1("Search by Ingredients"));
        conversation = new OpenAIConversation("demo", "gpt-4o-mini");
        TextField askRecipe = new TextField();
        Paragraph paragraph = new Paragraph();

        askRecipe.setPlaceholder("list some ingredients here");
        askRecipe.setWidth("75%");
        paragraph.setWidth("75%");
        paragraph.setHeight("auto");


        askRecipe.addKeyPressListener(Key.ENTER, event -> {
            ingridients = askRecipe.getValue();
            data = conversation.askQuestion("Can you show me a recipe with these ingredients ", ingridients);
            paragraph.setText(data);
        });

        add(askRecipe);
        add(paragraph);
    }
}

@Route(value = "about", layout = MainView.class)
@PageTitle("About")
class AboutView extends VerticalLayout {
    public AboutView() {
        add(new H1("About"));
        // Add your about view content here
    }
}