package one.digitalinnovation.santander.yasp.controller;

import one.digitalinnovation.santander.yasp.common.entity.BankCard;
import one.digitalinnovation.santander.yasp.common.entity.BankCardTypeEnum;
import one.digitalinnovation.santander.yasp.repository.BankCardRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BankCardController.class)
public class BankCardControllerTest {

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void setup() {
        BankCardRepository.get().clear();
    }

    @Test
    @DisplayName("Should receive not found when card does not exist")
    void shouldReceiveBadRequestWhenBankCardDoesNotExist() throws Exception {
        mockMvc.perform(get("/cards/{id}/tax", 1000L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return no taxes when it's a valid debit card")
    void shouldReturnNoTaxesWhenIsValidDebitCard() throws Exception {
        // Given
        final BankCard card = BankCard.builder().withId(1L).withType(BankCardTypeEnum.DEBIT).build();
        BankCardRepository.get().create(card);

        // When
        final ResultActions resultActions = mockMvc.perform(get("/cards/{id}/tax", 1L));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("tax").value(0D));
    }

    @Test
    @DisplayName("Should return taxes when it's a valid credit card")
    void shouldReturnNoTaxesWhenIsValidCreditCard() throws Exception {
        // Given
        final double monthlyTax = 10D;
        final BankCard card = BankCard.builder()
                .withId(1L)
                .withMonthlyTax(monthlyTax)
                .withType(BankCardTypeEnum.CREDIT)
                .build();
        BankCardRepository.get().create(card);

        // When
        final ResultActions resultActions = mockMvc.perform(get("/cards/{id}/tax", 1L));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("tax").value(monthlyTax));
    }

    @Test
    @DisplayName("Should return no taxes when it's a valid credit card of a customer with good relationship")
    void shouldReturnNoTaxesWhenIsValidCreditCardFromCustomerWithGoodRelationship() throws Exception {
        // Given
        final BankCard card = BankCard.builder()
                .withId(1L)
                .withTaxFreeForRelationShip(true)
                .withMonthlyTax(10D)
                .withType(BankCardTypeEnum.CREDIT)
                .build();
        BankCardRepository.get().create(card);

        // When
        final ResultActions resultActions = mockMvc.perform(get("/cards/{id}/tax", 1L));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("tax").value(0D));
    }

    @Test
    @DisplayName("Should return no taxes when it's a valid credit card and the customer has used it this month.")
    void shouldReturnNoTaxesWhenIsValidCreditCardAndCustomerHasUsedItThisMonth() throws Exception {
        // Given
        final BankCard card = BankCard.builder()
                .withId(1L)
                .withMonthlyTax(10D)
                .withType(BankCardTypeEnum.CREDIT)
                .build();
        BankCardRepository.get().create(card);
        BankCardRepository.get().addEntry(card.getId(), LocalDateTime.now(), 100D);

        // When
        final ResultActions resultActions = mockMvc.perform(get("/cards/{id}/tax", 1L));

        // Then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("id").value(1L))
                .andExpect(jsonPath("tax").value(0D));
    }
}
