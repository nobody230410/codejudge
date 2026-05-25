# 时序图（PlantUML）

| 文件 | 内容 |
|------|------|
| `seq-auth.puml` | 登录认证：注册、登录、登出 |
| `seq-regular-user.puml` | 普通用户常规业务：JWT、题库、个人账号 |
| `seq-regular-admin.puml` | 管理员常规业务：JWT、题目/用例、用户、提交查询 |
| `seq-submission-judge.puml` | 代码提交与判题（异步） |
| `seq-code-run.puml` | 代码在线运行（异步） |

正文 LaTeX 见 **`section-regular.tex`**（含 `subsubsection` 与 `pdfpages` 插图）。

## 生成图片

```bash
java -jar plantuml.jar -tpdf docs/latex/sequence/seq-auth.puml
java -jar plantuml.jar -tpdf docs/latex/sequence/seq-regular-user.puml
java -jar plantuml.jar -tpdf docs/latex/sequence/seq-regular-admin.puml
java -jar plantuml.jar -tpdf docs/latex/sequence/seq-submission-judge.puml
java -jar plantuml.jar -tpdf docs/latex/sequence/seq-code-run.puml
```

输出放入 `figs/sequence/`。多页 PDF 插入 LaTeX 见 `sequence-pdfpages.tex`（`\SeqPdfFigure`）。
