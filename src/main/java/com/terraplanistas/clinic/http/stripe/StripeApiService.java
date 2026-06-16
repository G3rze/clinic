package com.terraplanistas.clinic.http.stripe;

import com.terraplanistas.clinic.http.base.BaseHttpService;
import org.springframework.web.client.RestClient;

public abstract class StripeApiService extends BaseHttpService {

    protected StripeApiService(RestClient restClient, String baseUrl, String authToken) {
        super(restClient, baseUrl, authToken);
    }

    @Override
    protected String getProviderName() {
        return "STRIPE";
    }
}
