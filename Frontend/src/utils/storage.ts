const TOKEN_STORAGE_KEY = "hre_token";

export const storage = {
  getToken: (): string | null => localStorage.getItem(TOKEN_STORAGE_KEY),
  setToken: (token: string) => localStorage.setItem(TOKEN_STORAGE_KEY, token),
  clearToken: () => localStorage.removeItem(TOKEN_STORAGE_KEY),
};

export { TOKEN_STORAGE_KEY };