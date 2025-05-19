interface Token {
  accessToken: string;
}

export type TokenResponse = Token;

export type AdminLoginRequest = {
  username: string;
  password: string;
};

export type AdminLoginResponse = {
  accessToken: string;
  refreshToken: null;
  expiresIn: number;
  tokenType: string;
  issuedAt: string;
};
