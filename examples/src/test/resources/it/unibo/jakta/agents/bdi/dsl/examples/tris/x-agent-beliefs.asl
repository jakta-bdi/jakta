vertical([cell(X, Y, S)]) :- cell(X, Y, S).
vertical([cell(X, Y1, S1), cell(X, Y2, S2) | Cs]) :-
    cell(X, Y1, S1) & cell(X, Y2, S2) & Y2 - Y1 = 1 &
    vertical([cell(X, Y2, S2) | Cs]).

horizontal([cell(X, Y, S)]) :- cell(X, Y, S).
horizontal([cell(X1, Y, S1), cell(X2, Y, S2) | Cs]) :-
    cell(X1, Y, S1) & cell(X2, Y, S2) & X2 - X1 = 1 & 
    horizontal([cell(X2, Y, S2) | Cs]).

diagonal([cell(X, Y, S)]) :- cell(X, Y, S).
diagonal([cell(X1, Y1, S1), cell(X2, Y2, S2) | Cs]) :-
    cell(X1, Y1, S1) & cell(X2, Y2, S2) & X2 - X1 = 1 & Y2 - Y1 = 1 &
    diagonal([cell(X2, Y2, S2) | Cs]).

antidiagonal([cell(X, Y, S)]) :- cell(X, Y, S).
antidiagonal([cell(X1, Y1, S1), cell(X2, Y2, S2) | Cs]) :-
    cell(X1, Y1, S1) & cell(X2, Y2, S2) & X2 - X1 = 1 & Y1 - Y2 = 1 &
    antidiagonal([cell(X2, Y2, S2) | Cs]).

aligned(L) :- vertical(L) | horizontal(L) | diagonal(L) | antidiagonal(L).
