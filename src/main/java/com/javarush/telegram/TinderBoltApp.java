package com.javarush.telegram;

import com.javarush.telegram.ChatGPTService;
import com.javarush.telegram.DialogMode;
import com.javarush.telegram.MultiSessionTelegramBot;
import com.javarush.telegram.UserInfo;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;

public class TinderBoltApp extends MultiSessionTelegramBot {
  //  public static final String TELEGRAM_BOT_NAME = "t.me/ProstoPolin_bot"; //TODO: добавь имя бота в кавычках
  //  public static final String TELEGRAM_BOT_TOKEN = "7895914555:AAFjvSf1QWPIhnfacIkGYeLU3oWAhZ9sYvw"; //TODO: добавь токен бота в кавычках
  //  public static final String OPEN_AI_TOKEN = "gpt:GMEpATyDssZWFB4H2wdsJFkblB3TtbsOsfleLvmgKt8qL2qt"; //TODO: добавь токен ChatGPT в кавычках

    private ChatGPTService chatGPT = new ChatGPTService(OPEN_AI_TOKEN);

    private DialogMode currentMode = null;
    private ArrayList<String> list = new ArrayList<>();

    private UserInfo me;
    private UserInfo she;

    private int questionCount;

    public TinderBoltApp() {
        super(TELEGRAM_BOT_NAME, TELEGRAM_BOT_TOKEN);
    }

    @Override
    public void onUpdateEventReceived(Update update) {
        //TODO: основной функционал бота будем писать здесь
        String message = getMessageText();

        if (message.equals("/start")) {
            currentMode = DialogMode.MAIN;
            sendPhotoMessage("main");
            String text = loadMessage("main");
            sendTextMessage(text);

            showMainMenu(
                    "начало", "/start",
                    "Общение с GPT", "/gpt",
                    "генерация Tinder-профля", "/profile",
                    "сообщение для знакомства", "/opener",
                    "переписка от вашего имени ", "/message",
                    "переписка со звездами", "/date");
            return;
        }

        if (message.equals("/gpt")) {
            currentMode = DialogMode.GPT;
            sendPhotoMessage("gpt");
            String text = loadMessage("gpt");
            sendTextMessage(text);
            return;
        }

        if (currentMode == DialogMode.GPT && !isMessageCommand()) {
            String prompt = loadPrompt(("gpt"));

            Message nsg = sendTextMessage("Подождите, чат GPT набирает текст ...");
            String answer = chatGPT.sendMessage(prompt, message);
            updateTextMessage(nsg, answer);
            return;
        }

        //comand Date
        if (message.equals("/date")) {
            currentMode = DialogMode.DATE;
            sendPhotoMessage("date");
            String text = loadMessage("date");
            sendTextButtonsMessage(text,
                    "Ариана Гранде", "date_grande",
                    "Марго Робби", "date_robbie",
                    "Зендер", "date_zendeya",
                    "Райн Гослинг", "date_gosling",
                    "Том Харди", "date_hardy");
            return;
        }

        if (currentMode == DialogMode.DATE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("date_")) {
                sendPhotoMessage(query);
                sendTextMessage("отличный выбор \uD83D\uDE04");

                String prompt = loadPrompt(query);
                chatGPT.setPrompt(prompt);
                return;
            }
            Message msg = sendTextMessage("Подождите, девушка набирает текст ...");
            String answer = chatGPT.addMessage(message);
            updateTextMessage(msg, answer);
            return;
        }

        //command message
        if (message.equals("/message")) {
            currentMode = DialogMode.MESSAGE;
            sendPhotoMessage("message");
            sendTextButtonsMessage("Пришлите вашу переписку",
                    "Следующее сообщение", "message_next",
                    "Пригласить на свидание", "message_date");
            return;
        }

        if (currentMode == DialogMode.MESSAGE && !isMessageCommand()) {
            String query = getCallbackQueryButtonKey();
            if (query.startsWith("message_")) {
                String prompt = loadPrompt(query);
                String userChatHistory = String.join("\n\n", list);

                String answer = chatGPT.sendMessage(prompt, userChatHistory);
                sendTextMessage(answer);
                return;
            }
            list.add(message);
            return;
        }

        //command profile
        if (message.equals(("/profile"))) {
            currentMode = DialogMode.PROFILE;
            sendPhotoMessage("profile");

            me = new UserInfo();
            questionCount = 1;
            sendTextMessage("Сколько вам лет?");

            return;
        }

        //command PROFILE
        if (currentMode == DialogMode.PROFILE && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    me.age = message;
                    questionCount = 2;
                    sendTextMessage("кем ты работаешь? ");
                    return;
                case 2:
                    me.occupation = message;
                    questionCount = 3;
                    sendTextMessage("У вас профессия? ");
                    return;
                case 3:
                    me.hobby = message;
                    questionCount = 4;
                    sendTextMessage("Какое у вас хобби?");
                    return;
                case 4:
                    me.annoys = message;
                    questionCount = 5;
                    sendTextMessage("Цель вашего знакомства?");
                    return;
                case 5:
                    me.goals = message;
                    String aboutMyself = me.toString();
                    String prompt = loadPrompt("profile");

                    Message msg = sendTextMessage("Подождите, чат GPT \uD83D\uDE06 думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutMyself);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

        //command OPENER
        if (message.equals("/opener")) {
            currentMode = DialogMode.OPENER;
            sendPhotoMessage("opener");

            she = new UserInfo();
            questionCount = 1;
            sendTextMessage("Имя девушки?");
            return;
        }
        if (currentMode == DialogMode.OPENER && !isMessageCommand()) {
            switch (questionCount) {
                case 1:
                    she.name = message;

                    questionCount = 2;
                    sendTextMessage("Сколько ей лет?");
                    return;
                case 2:
                    she.name = message;

                    questionCount = 3;
                    sendTextMessage("Есть ли у нее хобби? какое?");
                    return;
                case 3:
                    she.name = message;

                    questionCount = 4;
                    sendTextMessage("Кем она работает?");
                    return;
                case 4:
                    she.name = message;

                    questionCount = 5;
                    sendTextMessage("Цель знакомства?");
                    return;
                case 5:
                    she.goals = message;

                    String aboutFriend = message;
                    String prompt = loadPrompt("opener");

                    Message msg = sendTextMessage("Подождите пару секунд - чат GPT думает...");
                    String answer = chatGPT.sendMessage(prompt, aboutFriend);
                    updateTextMessage(msg, answer);
                    return;
            }
            return;
        }

        sendTextMessage("*Ооо.. привет!*");
        sendTextMessage("Это вы написали? " + message);
        sendTextButtonsMessage("Выбери режим работы: ",
                "старт", "start",
                "стоп", "stop");
    }

    public static void main(String[] args) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(new TinderBoltApp());
    }
}
