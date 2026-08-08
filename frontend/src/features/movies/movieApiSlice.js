import { createApi } from '@reduxjs/toolkit/query/react';
import { baseQueryWithAuth } from '../../utils/baseQuery';

export const movieApiSlice = createApi({
  reducerPath: 'movieApi',
  baseQuery: baseQueryWithAuth,
  tagTypes: ['Movie'],
  endpoints: (builder) => ({
    getNowShowing: builder.query({
      query: () => '/movies/now-showing',
      transformResponse: (response) => response.data,
      providesTags: ['Movie'],
    }),
    getUpcoming: builder.query({
      query: () => '/movies/upcoming',
      transformResponse: (response) => response.data,
      providesTags: ['Movie'],
    }),
    getMovieById: builder.query({
      query: (id) => `/movies/${id}`,
      transformResponse: (response) => response.data,
      providesTags: (result, error, id) => [{ type: 'Movie', id }],
    }),
    searchMovies: builder.query({
      query: (q) => `/movies/search?q=${q}`,
      transformResponse: (response) => response.data,
      providesTags: ['Movie'],
    }),
    getAllMovies: builder.query({
      query: () => '/movies',
      transformResponse: (response) => response.data,
      providesTags: ['Movie'],
    }),
    createMovie: builder.mutation({
      query: (movieData) => ({
        url: '/movies',
        method: 'POST',
        body: movieData,
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: ['Movie'],
    }),
    updateMovie: builder.mutation({
      query: ({ id, ...movieData }) => ({
        url: `/movies/${id}`,
        method: 'PUT',
        body: movieData,
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: (result, error, { id }) => [{ type: 'Movie', id }, 'Movie'],
    }),
    deleteMovie: builder.mutation({
      query: (id) => ({
        url: `/movies/${id}`,
        method: 'DELETE',
      }),
      transformResponse: (response) => response.data,
      invalidatesTags: ['Movie'],
    }),
    toggleInterested: builder.mutation({
      query: (id) => ({
        url: `/movies/${id}/interested`,
        method: 'POST',
      }),
      invalidatesTags: (result, error, id) => [{ type: 'Movie', id }]
    }),
  }),
});

export const {
  useGetNowShowingQuery,
  useGetUpcomingQuery,
  useGetMovieByIdQuery,
  useSearchMoviesQuery,
  useGetAllMoviesQuery,
  useCreateMovieMutation,
  useUpdateMovieMutation,
  useDeleteMovieMutation,
  useToggleInterestedMutation
} = movieApiSlice;
