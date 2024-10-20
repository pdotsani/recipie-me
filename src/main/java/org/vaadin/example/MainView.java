package org.vaadin.example;

import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.RouterLink;
import ai.peoplecode.OpenAIConversation;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * A sample Vaadin view class.
 * <p>
 * To implement a Vaadin view just extend any Vaadin component and use @Route
 * annotation to announce it in a URL as a Spring managed bean.
 * <p>
 * A new instance of super class is created for every new user and every browser
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
        H1 logo = new H1("Recipe Me");
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

class AppView extends VerticalLayout {
    private String OPENAIKEY = (EnvUtils.get("OPEN_AI_KEY") != null) ? EnvUtils.get("OPEN_AI_KEY") : "demo";
    private OpenAIConversation conversation = new OpenAIConversation(OPENAIKEY, "gpt-4o-mini");
    private OpenAIImageGen imageGen = new OpenAIImageGen();
    private String recipe = "";
    private String data = "";
    private String time = "";
    private Div topDiv = new Div();
    private Div recipieDiv = new Div();
    private Div cookingTime = new Div();
    private Image foodImage = new Image();

    public Div getTopDiv() {
        return this.topDiv;
    }

    public Div getRecipieDiv() {
        return this.recipieDiv;
    }

    public Div getCookingTime() {
        return this.cookingTime;
    }

    public Image getFoodImage() {
        return this.foodImage;
    }

    public void setLayout() {
        this.foodImage.setWidth("25%");
        this.foodImage.setHeight("25%");
        this.cookingTime.setWidth("75%");
        this.topDiv.setWidth("75%");
        this.topDiv.getStyle().set("display", "flex");
        this.topDiv.getStyle().set("flexDirection", "row");
        this.topDiv.add(foodImage);
        this.topDiv.add(cookingTime);
        this.recipieDiv.setWidth("75%");
        this.recipieDiv.getStyle().set("min-height", "100px");
    }

    public OpenAIConversation getConversation() {
        return this.conversation;
    }

    public OpenAIImageGen getImageGen() {
        return this.imageGen;
    }

    public void setRecipe(String recipe) {
        this.recipe = recipe;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRecipe() {
        return this.recipe;
    }

    public String getData() {
        return this.data;
    }

    public String getTime() {
        return this.time;
    }
}

@Route(value = "recipe", layout = MainView.class)
@PageTitle("Recipe")
class RecipeView extends AppView {
    public RecipeView() {
        add(new H1("Search by Recipe"));
        TextField askRecipe = new TextField();

        askRecipe.setPlaceholder("enter a recipe here");
        askRecipe.setWidth("75%");
        super.setLayout();

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        askRecipe.addKeyPressListener(Key.ENTER, event -> {
            super.setRecipe(askRecipe.getValue());
            String data = super.getConversation().askQuestion("Can you show me a recipe for ", super.getRecipe() + ". Prefer just the information");
            super.setData(data);
            String time = super.getConversation().askQuestion(super.getRecipe(), "Can you show me prep time, cooking time, and total cooking time for? Prefer just the information.");
            super.setTime(time);

            Node documment = parser.parse(data);
            String html = renderer.render(documment);

            Node doc = parser.parse(time);
            String html2 = renderer.render(doc);

            super.getFoodImage().setSrc(super.getImageGen().generate(super.getRecipe()));

            super.getRecipieDiv().getElement().setProperty("innerHTML", html);
            super.getCookingTime().getElement().setProperty("innerHTML", html2);
        });

        add(askRecipe);
        add(super.getTopDiv());
        add(super.getRecipieDiv());
    }
}

@Route(value = "ingredients", layout = MainView.class)
@PageTitle("Ingredients")
class IngredientsView extends AppView {
    public IngredientsView() {
        add(new H1("Search by Ingredients"));
        TextField askRecipe = new TextField();

        askRecipe.setPlaceholder("list some ingredients here");
        askRecipe.setWidth("75%");
        super.setLayout();

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        askRecipe.addKeyPressListener(Key.ENTER, event -> {
            String ingridients = "";
            ingridients = askRecipe.getValue();

            String recipe = super.getConversation().askQuestion("Can you tell me the name of a recipe with these ingredients ", ingridients);
            super.setRecipe(recipe);
            String data = super.getConversation().askQuestion(super.getRecipe(),  "Can you tell me the recipe for super? Prefer just the information.");
            super.setData(data);
            String time = super.getConversation().askQuestion(super.getRecipe(),  "Can you show me prep time, cooking time, and total cooking time?Prefer just the information.");
            super.setTime(time);

            Node document = parser.parse(data);
            String html = renderer.render(document);

            Node doc = parser.parse(time);
            String html2 = renderer.render(doc);

            super.getFoodImage().setSrc(super.getImageGen().generate(recipe));

            super.getRecipieDiv().getElement().setProperty("innerHTML", html);
            super.getCookingTime().getElement().setProperty("innerHTML", html2);
        });

        add(askRecipe);
        add(super.getTopDiv());
        add(super.getRecipieDiv());
    }
}

@Route(value = "about", layout = MainView.class)
@PageTitle("About")
class AboutView extends VerticalLayout {
    public AboutView() {
        add(new H1("About"));
        add(new Anchor("https://github.com/pdotsani/recipe-me", "https://github.com/pdotsani/recipe-me"));
    }
}