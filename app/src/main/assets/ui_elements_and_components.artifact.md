# Celestia UI Elements & Components Structure

## 1. Navigation & Bars
- **`CelestiaTopBar`**: Floating glass header containing mascot state, title, network connectivity status, tokens-per-second throughput badge, search toggle, and settings button.
- **`CelestiaGlassBottomBar`**: Floating pill bottom navigation bar supporting fluid tab switching across `Hub`, `Chat`, `Scanner`, `Summarize`, `Translate`, and `Workbench`.

---

## 2. Core Screens & Progressive Disclosure
- **`HubScreen`**: Central command center presenting rich tool cards for all AI suite features, with integrated model selectors and quick-launch actions.
- **`ChatScreen`**: Conversational reasoning canvas featuring:
  - Clean two-row active chat header (Row 1: `ModelSelectorChip` + Web Search toggle; Row 2: Context Memory Usage progress bar with exact token counts).
  - Message items supporting Markdown, code execution, read-aloud voice synthesis, and cross-tool sharing actions (`CelestiaSendToActionBar`).
- **`ScannerScreen`**: Document and image OCR scanner with gallery/PDF file pickers, real-time extraction status, Markdown view, Microsoft Word (`.doc`) export, and "Send to..." cross-tool routing.
- **`SummarizeScreen`**: Executive brief and bullet-point generation canvas with style selectors and quick starter prompts.
- **`TranslateScreen`**: Multi-lingual neural translation canvas with language swapping and tone selectors (`Natural`, `Formal`, `Casual`, `Technical`).
- **`WorkbenchScreen`**: Advanced prompt engineering and code generation lab featuring presets (Math Solver, Code Refactor, JSON Extractor, Email Writer) and collapsible advanced parameters (Temperature, Top-P, Repetition Penalty, Custom System Prompts).

---

## 3. Data Visualization & Output Components
- **`CelestiaExcelTableView`**: Renders markdown tables as scrollable spreadsheet grids with sticky headers, alternating row colors, and CSV export.
- **`CelestiaBarChartView`**: Renders animated vertical bar charts with gradient bars for numeric table data.
- **`CelestiaMarkdownView`**: Modularized markdown parser supporting headers, bullet points, LaTeX math blocks, executable Python code blocks, and web link preview cards.
