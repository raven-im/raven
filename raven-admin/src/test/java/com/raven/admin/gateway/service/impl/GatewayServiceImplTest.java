package com.raven.admin.gateway.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.raven.common.param.OutTokenInfoParam;
import com.raven.common.result.Result;
import com.raven.common.utils.Constants;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@RunWith(MockitoJUnitRunner.class)
public class GatewayServiceImplTest {

    private static final String UID = "user-1";
    // DES requires an 8-byte key, so the app key (used as the cipher secret) must be >= 8 bytes.
    private static final String APP_KEY = "appsecret";
    private static final String USER_TOKEN_KEY = Constants.USER_TOKEN + APP_KEY + ":" + UID;

    @InjectMocks
    private GatewayServiceImpl service;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Before
    public void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    public void getTokenReusesCachedTokenWithoutRegenerating() {
        String cached = "cached-token";
        when(valueOperations.get(USER_TOKEN_KEY)).thenReturn(cached);
        when(stringRedisTemplate.hasKey(cached)).thenReturn(true);

        Result result = service.getToken(UID, APP_KEY);

        OutTokenInfoParam info = (OutTokenInfoParam) result.getData();
        assertEquals("an existing token must be reused", cached, info.getToken());
        // No new token is written when a valid one is cached.
        verify(valueOperations, never()).set(any(), any(), anyLong(), any(TimeUnit.class));
        // The single token's lifetime is refreshed instead.
        verify(stringRedisTemplate).expire(eq(USER_TOKEN_KEY), anyLong(), any(TimeUnit.class));
        verify(stringRedisTemplate).expire(eq(cached), anyLong(), any(TimeUnit.class));
    }

    @Test
    public void getTokenCreatesAndIndexesTokenOnCacheMiss() {
        when(valueOperations.get(USER_TOKEN_KEY)).thenReturn(null);

        Result result = service.getToken(UID, APP_KEY);

        OutTokenInfoParam info = (OutTokenInfoParam) result.getData();
        assertNotNull(info.getToken());

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
        // Both the token->user and user->token entries are written.
        verify(valueOperations, times(2))
            .set(keyCaptor.capture(), valueCaptor.capture(), anyLong(), any(TimeUnit.class));

        int reverseIndex = keyCaptor.getAllValues().indexOf(USER_TOKEN_KEY);
        assertEquals("reverse index must store the generated token",
            info.getToken(), valueCaptor.getAllValues().get(reverseIndex));
    }

    @Test
    public void getTokenDropsStaleTokenBeforeIssuingNewOne() {
        String stale = "stale-token";
        when(valueOperations.get(USER_TOKEN_KEY)).thenReturn(stale);
        when(stringRedisTemplate.hasKey(stale)).thenReturn(false);

        service.getToken(UID, APP_KEY);

        // The previous token is removed so a user never accumulates multiple tokens.
        verify(stringRedisTemplate).delete(stale);
    }
}
