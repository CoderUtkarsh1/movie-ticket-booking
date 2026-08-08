import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQueryWithAuth } from '../../utils/baseQuery';

export const notificationApiSlice = createApi({
  reducerPath: 'notificationApi',
  baseQuery: baseQueryWithAuth,
  tagTypes: ['Notification'],
  endpoints: (builder) => ({
    getNotifications: builder.query({
      query: () => '/notifications',
      transformResponse: (response) => response.data,
      providesTags: ['Notification'],
    }),
    getUnreadCount: builder.query({
      query: () => '/notifications/unread-count',
      transformResponse: (response) => response.data,
      providesTags: ['Notification'],
    }),
    markAsRead: builder.mutation({
      query: (id) => ({
        url: `/notifications/${id}/read`,
        method: 'PUT',
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: ['Notification'],
    }),
  }),
});

export const {
  useGetNotificationsQuery,
  useGetUnreadCountQuery,
  useMarkAsReadMutation,
} = notificationApiSlice;
