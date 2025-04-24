import { defineConfig } from 'eslint/config';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import unusedImports from 'eslint-plugin-unused-imports';
import _import from 'eslint-plugin-import';
import { fixupPluginRules } from '@eslint/compat';
import path from 'node:path';
import { fileURLToPath } from 'node:url';
import js from '@eslint/js';
import { FlatCompat } from '@eslint/eslintrc';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const compat = new FlatCompat({
  baseDirectory: __dirname,
  recommendedConfig: js.configs.recommended,
  allConfig: js.configs.all,
});

export default defineConfig([
  {
    files: ['**/*.{ts,tsx}'],

    extends: compat.extends(
      'next/core-web-vitals',
      'next/typescript',
      'prettier',
      'plugin:storybook/recommended',
    ),

    plugins: {
      'react-refresh': reactRefresh,
      'unused-imports': unusedImports,
    },

    rules: {
      'react-hooks/rules-of-hooks': 'error',
      'react-hooks/exhaustive-deps': 'warn',

      'react-refresh/only-export-components': [
        'warn',
        {
          allowConstantExport: true,
        },
      ],

      'unused-imports/no-unused-imports': 'warn',

      'unused-imports/no-unused-vars': [
        'warn',
        {
          vars: 'all',
          varsIgnorePattern: '^',
          args: 'after-used',
          argsIgnorePattern: '^',
        },
      ],

      // 빌드 오류 확인을 위해 비활성화
      '@typescript-eslint/no-unused-vars': 'off', // 사용하지 않는 변수 무시
      '@next/next/no-img-element': 'off', // img 태그 사용 무시

      'import/order': [
        'error',
        {
          groups: [
            'builtin',
            'external',
            'internal',
            'parent',
            'sibling',
            'index',
            'object',
            'type',
          ],

          alphabetize: {
            order: 'asc',
            caseInsensitive: true,
          },

          pathGroups: [
            {
              pattern: '@/',
              group: 'internal',
            },
          ],

          pathGroupsExcludedImportTypes: ['builtin'],
        },
      ],
    },
  },
]);
