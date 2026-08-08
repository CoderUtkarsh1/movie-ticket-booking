import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQueryWithAuth } from '../../utils/baseQuery';

export const theaterApiSlice = createApi({
  reducerPath: 'theaterApi',
  baseQuery: baseQueryWithAuth,
  tagTypes: ['Theater', 'Screen'],
  endpoints: (builder) => ({
    getTheaters: builder.query({
      query: (city) => (city ? `/theaters?city=${city}` : '/theaters'),
      transformResponse: (response) => response.data,
      providesTags: ['Theater'],
    }),
    getTheaterById: builder.query({
      query: (id) => `/theaters/${id}`,
      transformResponse: (response) => response.data,
      providesTags: (result, error, id) => [{ type: 'Theater', id }],
    }),
    createTheater: builder.mutation({
      query: (theaterData) => ({
        url: '/theaters',
        method: 'POST',
        body: theaterData,
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: ['Theater'],
    }),
    getScreens: builder.query({
      query: (theaterId) => `/theaters/${theaterId}/screens`,
      transformResponse: (response) => response.data,
      providesTags: (result, error, id) => [{ type: 'Screen', id }],
    }),
    addScreen: builder.mutation({
      query: ({ theaterId, ...screenData }) => ({
        url: `/theaters/${theaterId}/screens`,
        method: 'POST',
        body: screenData,
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: (result, error, { theaterId }) => [{ type: 'Screen', id: theaterId }],
    }),
  }),
});

export const {
  useGetTheatersQuery,
  useGetTheaterByIdQuery,
  useCreateTheaterMutation,
  useGetScreensQuery,
  useAddScreenMutation,
} = theaterApiSlice;
