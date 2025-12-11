package com.kirylliuss.telegram.bot.transportBot.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * The class is a configuration class for telegram bot.
 * It uses for getting bot name and token.
 * from application.properties
 * <p>Important!: The token must be set by .env</p>
 * @author kirylliuss
 * @since 1.0
 */

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    /**
     * Bot's name returns from {@code String name;}
     * Rows are not marked as {@code private} because Lombok usage
     * Name extracts from {@code bot.name} variable in application.properties
     */

    @Value("${bot.name}")
    String name;

    /**
     * Bot's token returns from {@code String token;}
     * Rows are not marked as {@code private} because Lombok usage
     * Token extracts from {@code bot.token} variable in application.properties
     */

    @Value("${bot.token}")
    String token;
}
