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

class AppView extends VerticalLayout {
    private String recipe = "";
    private String data = "";
    private String time = "";

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
        return recipe;
    }

    public String getData() {
        return data;
    }

    public String getTime() {
        return time;
    }
}

@Route(value = "recipe", layout = MainView.class)
@PageTitle("Recipe")
class RecipeView extends AppView {
    private final OpenAIConversation conversation;
    private final OpenAIImageGen imageGen;

    public RecipeView() {
        String OPENAIKEY = (EnvUtils.get("OPEN_AI_KEY") != null) ? EnvUtils.get("OPEN_AI_KEY") : "demo";
        conversation = new OpenAIConversation(OPENAIKEY, "gpt-4o-mini");
        imageGen = new OpenAIImageGen();
        add(new H1("Search by Recipe"));
        TextField askRecipe = new TextField();
        Div topDiv = new Div();
        Div recipieDiv = new Div();
        Div cookingTime = new Div();
        Image foodImage = new Image();

        askRecipe.setPlaceholder("enter a recipe here");
        askRecipe.setWidth("75%");
        foodImage.setWidth("25%");
        foodImage.setHeight("25%");
        cookingTime.setWidth("75%");
        topDiv.setWidth("75%");
        topDiv.getStyle().set("display", "flex");
        topDiv.getStyle().set("flexDirection", "row");
        topDiv.add(foodImage);
        topDiv.add(cookingTime);
        recipieDiv.getStyle().set("min-height", "100px");
        recipieDiv.setWidth("75%");
        recipieDiv.getStyle().set("min-height", "100px");

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        askRecipe.addKeyPressListener(Key.ENTER, event -> {
            this.setRecipe(askRecipe.getValue());
            String data = conversation.askQuestion("Can you show me a recipe for ", this.getRecipe() + ". Prefer just the information");
            this.setData(data);
            String time = conversation.askQuestion(this.getRecipe(), "Can you show me prep time, cooking time, and total cooking time for? Prefer just the information.");
            this.setTime(time);

            Node documment = parser.parse(data);
            String html = renderer.render(documment);

            Node doc = parser.parse(time);
            String html2 = renderer.render(doc);

            foodImage.setSrc(imageGen.generate(this.getRecipe()));

            recipieDiv.getElement().setProperty("innerHTML", html);
            cookingTime.getElement().setProperty("innerHTML", html2);
        });

        add(askRecipe);
        add(topDiv);
        add(recipieDiv);
    }
}

@Route(value = "ingredients", layout = MainView.class)
@PageTitle("Ingredients")
class IngredientsView extends AppView {
    private final OpenAIConversation conversation;
    private final OpenAIImageGen imageGen;
    private String ingridients = "";

    public IngredientsView() {
        String OPENAIKEY = (EnvUtils.get("OPEN_AI_KEY") != null) ? EnvUtils.get("OPEN_AI_KEY") : "demo";
        add(new H1("Search by Ingredients"));
        conversation = new OpenAIConversation(OPENAIKEY, "gpt-4o-mini");
        imageGen = new OpenAIImageGen();
        TextField askRecipe = new TextField();
        Div recipieDiv = new Div();
        Div cookingTime = new Div();
        Div topDiv = new Div();
        Image foodImage = new Image();

        askRecipe.setPlaceholder("list some ingredients here");
        foodImage.setWidth("25%");
        foodImage.setHeight("25%");
        cookingTime.setWidth("75%");
        askRecipe.setWidth("75%");
        recipieDiv.setWidth("75%");
        topDiv.getStyle().set("display", "flex");
        topDiv.getStyle().set("flexDirection", "row");
        topDiv.add(foodImage);
        topDiv.add(cookingTime);
        recipieDiv.getStyle().set("min-height", "100px");

        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        askRecipe.addKeyPressListener(Key.ENTER, event -> {
            ingridients = askRecipe.getValue();

            String recipe = conversation.askQuestion("Can you tell me the name of a recipe with these ingredients ", ingridients);
            this.setRecipe(recipe);
            String data = conversation.askQuestion(this.getRecipe(),  "Can you tell me the recipe for this? Prefer just the information.");
            this.setData(data);
            String time = conversation.askQuestion(this.getRecipe(),  "Can you show me prep time, cooking time, and total cooking time?Prefer just the information.");
            this.setTime(time);

            Node document = parser.parse(data);
            String html = renderer.render(document);

            Node doc = parser.parse(time);
            String html2 = renderer.render(doc);

            foodImage.setSrc(imageGen.generate(recipe));

            recipieDiv.getElement().setProperty("innerHTML", html);
            cookingTime.getElement().setProperty("innerHTML", html2);
        });

        add(askRecipe);
        add(topDiv);
        add(recipieDiv);
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