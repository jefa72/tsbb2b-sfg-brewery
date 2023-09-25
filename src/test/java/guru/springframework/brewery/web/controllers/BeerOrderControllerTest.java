package guru.springframework.brewery.web.controllers;

import guru.springframework.brewery.services.BeerOrderService;
import guru.springframework.brewery.web.model.*;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BeerOrderController.class)
class BeerOrderControllerTest {

    @MockBean
    BeerOrderService beerOrderService;

    @Autowired
    MockMvc mockMvc;

    BeerOrderDto beerOrderDto;

    UUID customerId = UUID.randomUUID();
    UUID orderId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        beerOrderDto = BeerOrderDto.builder()
                .id(orderId).orderStatus(OrderStatusEnum.NEW).customerId(customerId).build();
    }

    @AfterEach
    void tearDown() {
        reset(beerOrderService);
    }

    @DisplayName("List Ops - ")
    @Nested
    public class TestListOrdersOperations {

        @Captor
        ArgumentCaptor<UUID> customerIdCaptor;

        @Captor
        ArgumentCaptor<PageRequest> pageRequestCaptor;

        BeerOrderPagedList beerOrderPagedList;

        @BeforeEach
        void setUp() {
            List<BeerOrderDto> orders = new ArrayList<>();
            orders.add(beerOrderDto);

            beerOrderPagedList = new BeerOrderPagedList(orders);

            given(beerOrderService.listOrders(customerIdCaptor.capture(), pageRequestCaptor.capture())).willReturn(beerOrderPagedList);

        }

        @Test
        void testListOrders() throws Exception {
            mockMvc.perform(get("/api/v1/customers/{customerId}/orders", customerId)
                      .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].customerId", is(customerId.toString())))
                    .andExpect(jsonPath("$.content[0].id", is(orderId.toString())));

        }

    }

    @Test
    void getOrder() throws Exception {
        given(beerOrderService.getOrderById(any(), any())).willReturn(beerOrderDto);

        MvcResult result = mockMvc.perform(get("/api/v1/customers/" + customerId + "/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.id", is(beerOrderDto.getId().toString())))
                .andExpect(jsonPath("$.customerId", is(beerOrderDto.getCustomerId().toString())))
                .andReturn();
    }
}