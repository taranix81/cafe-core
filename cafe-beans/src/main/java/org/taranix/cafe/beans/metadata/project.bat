@echo off
setlocal enabledelayedexpansion

:: ----------------------------------------------------
:: NAZWA PLIKU WYJŚCIOWEGO
:: ----------------------------------------------------
set OUTPUT_FILE=project_sources.jps

:: ----------------------------------------------------
:: ROZSZERZENIA PLIKÓW
:: WAŻNE: Dodaj lub usuń rozszerzenia (bez kropki, oddzielone spacją)
:: ----------------------------------------------------
set EXTENSIONS=java c cpp h hpp py js cs sh html css ts

:: Usuń istniejący plik wyjściowy
if exist "%OUTPUT_FILE%" del "%OUTPUT_FILE%"

echo Rozpoczęto łączenie kodów źródłowych do %OUTPUT_FILE%
echo ----------------------------------------------------

:: Pętla zewnętrzna iteruje po wszystkich zdefiniowanych rozszerzeniach
for %%E in (%EXTENSIONS%) do (
    
    echo Szukam plików o rozszerzeniu: %%E
    
    :: Pętla wewnętrzna FOR /R przeszukuje rekurencyjnie pliki z bieżącym rozszerzeniem
    for /R %%F in (*.%%E) do (
        
        :: Sprawdź, czy znaleziony plik to nie jest nasz plik wyjściowy
        if not "%%~nxF" == "%OUTPUT_FILE%" (
            
            echo Przetwarzanie: %%F
            
            :: 1. Zapis nazwy pliku w formacie: Nazwa "### " + Pliku
            echo ### %%F >> "%OUTPUT_FILE%"
            
            :: 2. Zapis zawartości pliku
            type "%%F" >> "%OUTPUT_FILE%"
            
            :: 3. Dodanie znacznika rozróżnienia '#####' (z pustymi liniami)
            echo. >> "%OUTPUT_FILE%"
            echo ##### >> "%OUTPUT_FILE%"
            echo. >> "%OUTPUT_FILE%"
        )
    )
)

echo ----------------------------------------------------
echo Gotowe! Wszystkie pliki zostały połączone.
pause

endlocal