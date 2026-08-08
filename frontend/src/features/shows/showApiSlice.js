import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQueryWithAuth } from '../../utils/baseQuery';

export const showApiSlice = createApi({
  reducerPath: 'showApi',
  baseQuery: baseQueryWithAuth,
  tagTypes: ['Show', 'ShowSeat'],
  endpoints: (builder) => ({
    getShows: builder.query({
      query: ({ movieId, date }) => `/shows?movieId=${movieId}&date=${date}`,
      transformResponse: (response) => response.data,
      providesTags: ['Show'],
    }),
    getShowById: builder.query({
      query: (id) => `/shows/${id}`,
      transformResponse: (response) => response.data,
      providesTags: (result, error, id) => [{ type: 'Show', id }],
    }),
    createShow: builder.mutation({
      query: (showData) => ({
        url: '/shows',
        method: 'POST',
        body: showData,
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: ['Show'],
    }),
    getShowSeats: builder.query({
      query: (showId) => `/shows/${showId}/seats`,
      transformResponse: (response) => response.data,
      providesTags: (result, error, id) => [{ type: 'ShowSeat', id }],
    }),
    blockSeats: builder.mutation({
      query: ({ showId, seatIds, userId }) => ({
        url: `/shows/${showId}/seats/block`,
        method: 'PUT',
        body: { seatIds, userId },
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: (result, error, { showId }) => [{ type: 'ShowSeat', id: showId }],
    }),
  }),
});

export const {
  useGetShowsQuery,
  useGetShowByIdQuery,
  useCreateShowMutation,
  useGetShowSeatsQuery,
  useBlockSeatsMutation,
} = showApiSlice;
