package com.kirylliuss.telegram.bot.transportBot.telegram;

import com.kirylliuss.telegram.bot.transportBot.config.BotConfig;
import com.kirylliuss.telegram.bot.transportBot.mail.SendEmail;
import com.kirylliuss.telegram.bot.transportBot.state.UserState;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TelegramBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;

    private final Map<Long, UserState> userStates = new ConcurrentHashMap<>();

    private SendEmail sendEmail;

    public TelegramBot(BotConfig botConfig, SendEmail sendEmail) {
        this.botConfig = botConfig;
        this.sendEmail = sendEmail;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getName();
    }

    @Override
    public String getBotToken(){
        return botConfig.getToken();
    }

    private void startCommandReceived(long chatId, String name){
        String wellcomeMessage = "Прывітанкі, " + name + "!\n" +
                "Я - тэлеграм бот, які дапамагае атрымаць інфармацыю аб раскладзе грамадзянскага транспарту ў Мінске.";
        sendMessage(wellcomeMessage, chatId);
    }

    private void invalidInputMessage(long chatId, String message){
        String invalidInput = "Нажаль, але ж каманды \"" + message + "\" не існуе.\n" +
                "Каб пряглядзець спіс існуючых каманд напішыце \"/help\".";
        sendMessage(invalidInput, chatId);
    }

    private void showHelpMessage(long chatId){
        String helpMessage = "Існуючыя каманды і іх выкарыстанне:\n" +
                "/start - Выводзіць прывітанне\n" +
                "/help - Выводзіць гэты тэкст\n" +
                "Як што одна з каманд не працуе - /feedback і апішыце праблему.";
        sendMessage(helpMessage, chatId);
    }

    private void startFeedbackProcess(long chatId) {
        String message = "Калі ласка, увядзіце тэкст вашага водгуку. Мы яго запішам:";
        sendMessage(message, chatId);

        userStates.put(chatId, UserState.WAITING_FOR_FEEDBACK);
    }

    private void handleFeedbackMessage(long chatId, String feedbackText, String username) {

        saveFeedback(chatId, username, feedbackText);
        userStates.put(chatId, UserState.DEFAULT);
        String response = "Дзякуй за ваш водгук!";
        sendMessage(response, chatId);
    }

    private void saveFeedback(long chatId, String username, String feedbackText) {
        sendEmail.sendMessage("header = { " + chatId + ", " + username + " }\n" +
                "text = { " + feedbackText + " }.");
    }

    private void sendMessage(String message, long chatId){
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try{
            execute(sendMessage);
        } catch (TelegramApiException ex){

        }
    }

    @Override
    public void onUpdateReceived(Update update) {

        if(update.hasMessage() && update.getMessage().hasText()){
            String msgText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getChat().getFirstName();

            if (userStates.getOrDefault(chatId, UserState.DEFAULT) == UserState.WAITING_FOR_FEEDBACK) {
                handleFeedbackMessage(chatId, msgText, username);
                return;
            }

            switch (msgText){
                case "/start":
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName());
                    break;
                case "/help":
                    showHelpMessage(chatId);
                    break;
                case "/feedback":
                    startFeedbackProcess(chatId);
                    break;
                default:
                    invalidInputMessage(chatId, update.getMessage().getText());
                    break;
            }
        }
    }
}
