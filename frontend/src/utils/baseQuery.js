import { fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { API_BASE_URL } from './constants';

export const baseQueryWithAuth = fetchBaseQuery({
  baseUrl: API_BASE_URL,
  credentials: 'include', // Send httpOnly cookies with every request
  prepareHeaders: (headers) => {
    // Token is now in httpOnly cookie — no need to manually set Authorization header
    // The browser automatically sends the cookie with credentials: 'include'
    return headers;
  },
});
