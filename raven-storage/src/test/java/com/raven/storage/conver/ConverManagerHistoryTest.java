package com.raven.storage.conver;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.raven.common.model.MsgContent;
import com.raven.common.utils.JsonHelper;
import java.util.LinkedHashSet;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

@RunWith(MockitoJUnitRunner.class)
public class ConverManagerHistoryTest {

    private static final String CONVER_ID = "conver-1";
    private static final String MESSAGE_KEY = "msg_" + CONVER_ID;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ZSetOperations<String, String> zSetOperations;

    private ConverManager converManager;

    @Before
    public void setUp() {
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        converManager = new ConverManager(redisTemplate);
    }

    private LinkedHashSet<String> messages(long... ids) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (long id : ids) {
            set.add(JsonHelper.toJsonString(
                MsgContent.builder().id(id).uid("u").type(0).content("c").time(id).build()));
        }
        return set;
    }

    @Test
    public void pullDownHonoursRequestedCount() {
        when(zSetOperations.rangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
            .thenReturn(messages(11L, 12L));

        List<MsgContent> result = converManager.getHistoryMsg(CONVER_ID, 10L, 25, false);

        ArgumentCaptor<Long> count = ArgumentCaptor.forClass(Long.class);
        verify(zSetOperations)
            .rangeByScore(eq(MESSAGE_KEY), anyDouble(), anyDouble(), anyLong(), count.capture());
        assertEquals(25L, count.getValue().longValue());
        verify(zSetOperations, never())
            .reverseRangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong());
        assertEquals(2, result.size());
    }

    @Test
    public void pullUpUsesReverseRangeAndReturnsChronologicalOrder() {
        // Redis reverse range yields newest-first (9 then 8); result must be flipped to 8 then 9.
        when(zSetOperations
            .reverseRangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
            .thenReturn(messages(9L, 8L));

        List<MsgContent> result = converManager.getHistoryMsg(CONVER_ID, 10L, 2, true);

        verify(zSetOperations)
            .reverseRangeByScore(eq(MESSAGE_KEY), anyDouble(), anyDouble(), anyLong(), anyLong());
        verify(zSetOperations, never())
            .rangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong());
        assertEquals(8L, result.get(0).getId());
        assertEquals(9L, result.get(1).getId());
    }

    @Test
    public void nonPositiveCountFallsBackToDefault() {
        when(zSetOperations.rangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
            .thenReturn(messages());

        converManager.getHistoryMsg(CONVER_ID, 0L, 0, false);

        ArgumentCaptor<Long> count = ArgumentCaptor.forClass(Long.class);
        verify(zSetOperations)
            .rangeByScore(eq(MESSAGE_KEY), anyDouble(), anyDouble(), anyLong(), count.capture());
        assertEquals(ConverManager.DEFAULT_HISTORY_COUNT, count.getValue().longValue());
    }

    @Test
    public void oversizedCountIsCapped() {
        when(zSetOperations.rangeByScore(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
            .thenReturn(messages());

        converManager.getHistoryMsg(CONVER_ID, 0L, ConverManager.MAX_HISTORY_COUNT + 50, false);

        ArgumentCaptor<Long> count = ArgumentCaptor.forClass(Long.class);
        verify(zSetOperations)
            .rangeByScore(eq(MESSAGE_KEY), anyDouble(), anyDouble(), anyLong(), count.capture());
        assertEquals(ConverManager.DEFAULT_HISTORY_COUNT, count.getValue().longValue());
    }
}
