import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQueryWithAuth } from '../../utils/baseQuery';

export const bookingApiSlice = createApi({
  reducerPath: 'bookingApi',
  baseQuery: baseQueryWithAuth,
  tagTypes: ['Booking'],
  endpoints: (builder) => ({
    createBooking: builder.mutation({
      query: (bookingData) => ({
        url: '/bookings',
        method: 'POST',
        body: bookingData,
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: ['Booking'],
    }),
    getBookingById: builder.query({
      query: (id) => `/bookings/${id}`,
      transformResponse: (response) => response.data,
      providesTags: (result, error, id) => [{ type: 'Booking', id }],
    }),
    getUpcomingBookings: builder.query({
      query: () => '/bookings/dashboard/upcoming',
      transformResponse: (response) => response.data,
      providesTags: ['Booking'],
    }),
    getPastBookings: builder.query({
      query: () => '/bookings/dashboard/past',
      transformResponse: (response) => response.data,
      providesTags: ['Booking'],
    }),
    cancelBooking: builder.mutation({
      query: (id) => ({
        url: `/bookings/${id}/cancel`,
        method: 'PUT',
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: (result, error, id) => [{ type: 'Booking', id }, 'Booking'],
    }),
  }),
});

export const {
  useCreateBookingMutation,
  useGetBookingByIdQuery,
  useGetUpcomingBookingsQuery,
  useGetPastBookingsQuery,
  useCancelBookingMutation,
} = bookingApiSlice;
