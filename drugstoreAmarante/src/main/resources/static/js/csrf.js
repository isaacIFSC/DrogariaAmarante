function csrfHeaders(headers = {}) {
  const cookie = document.cookie
    .split('; ')
    .find((item) => item.startsWith('XSRF-TOKEN='));
  if (!cookie) return headers;
  return { ...headers, 'X-XSRF-TOKEN': decodeURIComponent(cookie.substring('XSRF-TOKEN='.length)) };
}
