import { createMiddleware } from '@mswjs/http-middleware';
import cors from 'cors';
import express from 'express';
import { handlers } from './handlers';

const app = express();
const PORT = 8080;

app.use(
  cors({
    origin: 'http://localhost:3000', // 클라이언트 주소
    optionsSuccessStatus: 200,
    credentials: true,
  }),
);
app.use(express.json());
app.use(createMiddleware(...handlers));

app.listen(PORT, () => console.log(`Mock server is running on port: ${PORT}`));
